/*******************************************************************************
 * Copyright (c) quickfixengine.org  All rights reserved. 
 * 
 * This file is part of the QuickFIX FIX Engine 
 * 
 * This file may be distributed under the terms of the quickfixengine.org 
 * license as defined by quickfixengine.org and appearing in the file 
 * LICENSE included in the packaging of this file. 
 * 
 * This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING 
 * THE WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A 
 * PARTICULAR PURPOSE. 
 * 
 * See http://www.quickfixengine.org/LICENSE for licensing information. 
 * 
 * Contact ask@quickfixengine.org if any conditions of this licensing 
 * are not clear to you.
 ******************************************************************************/

package quickfix.examples.executor;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quickfix.ConfigError;
import quickfix.FieldConvertError;
import quickfix.FieldNotFound;
import quickfix.IncorrectTagValue;
import quickfix.LogUtil;
import quickfix.Message;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionSettings;
import quickfix.UnsupportedMessageType;
import quickfix.examples.fix.builder.execution.ExecutionReportBuilder;
import quickfix.examples.fix.builder.execution.ExecutionReportBuilderFactory;
import quickfix.examples.utility.CrackedApplicationAdapter;
import quickfix.examples.utility.DefaultMessageSender;
import quickfix.examples.utility.IdGenerator;
import quickfix.examples.utility.MessageSender;
import quickfix.field.BeginString;
import quickfix.field.OrdStatus;
import quickfix.field.OrdType;
import quickfix.field.OrderQty;
import quickfix.field.Price;
import quickfix.field.Side;
import quickfix.field.Symbol;

public class Application extends CrackedApplicationAdapter {
	private static final String DEFAULT_MARKET_PRICE_KEY = "DefaultMarketPrice";
	private static final String ALWAYS_FILL_LIMIT_KEY = "AlwaysFillLimitOrders";
	private static final String VALID_ORDER_TYPES_KEY = "ValidOrderTypes";

	private final static Logger log = LoggerFactory
			.getLogger(Application.class);
	private final boolean alwaysFillLimitOrders;
	private final HashSet<String> validOrderTypes = new HashSet<String>();
	private MarketDataProvider marketDataProvider;
	private ExecutionReportBuilderFactory builderFactory = new ExecutionReportBuilderFactory();
	private MessageSender messageSender;
	private IdGenerator idGenerator = new IdGenerator();

	public Application(SessionSettings settings) throws ConfigError,
			FieldConvertError {
		this(settings, new DefaultMessageSender());
	}

	public Application(SessionSettings settings, MessageSender messageSender)
			throws ConfigError, FieldConvertError {
		String validOrderTypesStr = null;
		if (settings.isSetting(VALID_ORDER_TYPES_KEY)) {
			validOrderTypesStr = settings.getString(VALID_ORDER_TYPES_KEY)
					.trim();
		}

		double defaultMarketPrice = 0.0;
		if (settings.isSetting(DEFAULT_MARKET_PRICE_KEY)) {
			defaultMarketPrice = settings.getDouble(DEFAULT_MARKET_PRICE_KEY);
		}

		if (settings.isSetting(ALWAYS_FILL_LIMIT_KEY)) {
			this.alwaysFillLimitOrders = settings
					.getBool(ALWAYS_FILL_LIMIT_KEY);
		} else {
			this.alwaysFillLimitOrders = false;
		}
		initializeValidOrderTypes(validOrderTypesStr);
		initializeMarketDataProvider(defaultMarketPrice);
		this.messageSender = messageSender;
	}

	public Application(boolean alwaysFillLimitOrders,
			String validOrderTypesStr, double defaultMarketPrice,
			MessageSender messageSender) throws ConfigError, FieldConvertError {
		initializeValidOrderTypes(validOrderTypesStr);
		initializeMarketDataProvider(defaultMarketPrice);
		this.alwaysFillLimitOrders = alwaysFillLimitOrders;
		this.messageSender = messageSender;
	}

	private void initializeMarketDataProvider(final double defaultMarketPrice)
			throws ConfigError, FieldConvertError {
		if (defaultMarketPrice > 0.0) {
			if (marketDataProvider == null) {
				marketDataProvider = new MarketDataProvider() {
					public double getAsk(String symbol) {
						return defaultMarketPrice;
					}

					public double getBid(String symbol) {
						return defaultMarketPrice;
					}
				};
			} else {
				log.warn("Ignoring " + DEFAULT_MARKET_PRICE_KEY
						+ " since provider is already defined.");
			}
		}
	}

	private void initializeValidOrderTypes(String validOrderTypesStr)
			throws ConfigError, FieldConvertError {
		if (validOrderTypesStr != null) {
			List<String> orderTypes = Arrays.asList(validOrderTypesStr
					.split("\\s*,\\s*"));
			validOrderTypes.addAll(orderTypes);
		} else {
			validOrderTypes.add(OrdType.LIMIT + "");
		}
	}

	public void onCreate(SessionID sessionID) {
		Session.lookupSession(sessionID).getLog()
				.onEvent("Valid order types: " + validOrderTypes);
	}

	public void onMessage(quickfix.fix40.NewOrderSingle order,
			SessionID sessionID) throws FieldNotFound, UnsupportedMessageType,
			IncorrectTagValue {
		onNewOrder(order, sessionID);
	}

	public void onMessage(quickfix.fix41.NewOrderSingle order,
			SessionID sessionID) throws FieldNotFound, UnsupportedMessageType,
			IncorrectTagValue {
		onNewOrder(order, sessionID);
	}

	public void onMessage(quickfix.fix42.NewOrderSingle order,
			SessionID sessionID) throws FieldNotFound, UnsupportedMessageType,
			IncorrectTagValue {
		onNewOrder(order, sessionID);
	}

	public void onMessage(quickfix.fix43.NewOrderSingle order,
			SessionID sessionID) throws FieldNotFound, UnsupportedMessageType,
			IncorrectTagValue {
		onNewOrder(order, sessionID);
	}

	public void onMessage(quickfix.fix44.NewOrderSingle order,
			SessionID sessionID) throws FieldNotFound, UnsupportedMessageType,
			IncorrectTagValue {
		onNewOrder(order, sessionID);
	}

	public void onMessage(quickfix.fix50.NewOrderSingle order,
			SessionID sessionID) throws FieldNotFound, UnsupportedMessageType,
			IncorrectTagValue {
		onNewOrder(order, sessionID);
	}

	private void onNewOrder(Message order, SessionID sessionID)
			throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue {
		try {
			validateOrder(order);

			String beginStr = order.getHeader().getString(BeginString.FIELD);
			ExecutionReportBuilder builder = builderFactory
					.getExecutionReportBuilder(beginStr);

			OrderQty orderQty = new OrderQty();
			order.getField(orderQty);
			Price price = getPrice(order);
			String orderID = idGenerator.genOrderID();

			Message accept = builder.orderAcked(order, orderID,
					idGenerator.genExecID());
			this.messageSender.sendMessage(accept, sessionID);

			if (isOrderExecutable(order, price)) {
				Message fill = builder.fillOrder(order, orderID,
						idGenerator.genExecID(), OrdStatus.FILLED,
						orderQty.getValue(), price.getValue(),
						orderQty.getValue(), price.getValue());

				this.messageSender.sendMessage(fill, sessionID);
			}
		} catch (RuntimeException e) {
			LogUtil.logThrowable(sessionID, e.getMessage(), e);
		}
	}

	private boolean isOrderExecutable(Message order, Price price)
			throws FieldNotFound {
		if (order.getChar(OrdType.FIELD) == OrdType.LIMIT) {
			BigDecimal limitPrice = new BigDecimal(order.getString(Price.FIELD));
			char side = order.getChar(Side.FIELD);
			BigDecimal thePrice = new BigDecimal("" + price.getValue());

			return (side == Side.BUY && thePrice.compareTo(limitPrice) <= 0)
					|| ((side == Side.SELL || side == Side.SELL_SHORT) && thePrice
							.compareTo(limitPrice) >= 0);
		}
		return true;
	}

	private Price getPrice(Message message) throws FieldNotFound {
		if (message.getChar(OrdType.FIELD) == OrdType.LIMIT
				&& alwaysFillLimitOrders) {
			return new Price(message.getDouble(Price.FIELD));
		} else {
			if (marketDataProvider == null) {
				throw new RuntimeException(
						"No market data provider specified for market order");
			}
			char side = message.getChar(Side.FIELD);
			String symbol = message.getString(Symbol.FIELD);
			return quotePrice(side, symbol);
		}
	}

	private Price quotePrice(char side, String symbol) throws FieldNotFound {
		if (side == Side.BUY) {
			return new Price(marketDataProvider.getAsk(symbol));
		} else if (side == Side.SELL || side == Side.SELL_SHORT) {
			return new Price(marketDataProvider.getBid(symbol));
		} else {
			throw new RuntimeException("Invalid order side: " + side);
		}
	}

	private void validateOrder(Message order) throws IncorrectTagValue,
			FieldNotFound {
		OrdType ordType = new OrdType(order.getChar(OrdType.FIELD));
		if (!validOrderTypes.contains(Character.toString(ordType.getValue()))) {
			log.error("Order type not in ValidOrderTypes setting");
			throw new IncorrectTagValue(ordType.getField());
		}
		if (ordType.getValue() == OrdType.MARKET && marketDataProvider == null) {
			log.error("DefaultMarketPrice setting not specified for market order");
			throw new IncorrectTagValue(ordType.getField());
		}
	}

	/**
	 * Allows a custom market data provider to be specified.
	 * 
	 * @param marketDataProvider
	 */
	public void setMarketDataProvider(MarketDataProvider marketDataProvider) {
		this.marketDataProvider = marketDataProvider;
	}
}
