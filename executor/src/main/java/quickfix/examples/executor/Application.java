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
import quickfix.DataDictionaryProvider;
import quickfix.DoNotSend;
import quickfix.FieldConvertError;
import quickfix.FieldNotFound;
import quickfix.FixVersions;
import quickfix.IncorrectDataFormat;
import quickfix.IncorrectTagValue;
import quickfix.LogUtil;
import quickfix.Message;
import quickfix.MessageUtils;
import quickfix.RejectLogon;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionNotFound;
import quickfix.SessionSettings;
import quickfix.UnsupportedMessageType;
import quickfix.examples.fix.builder.execution.FIX40ExecutionReportBuilder;
import quickfix.examples.fix.builder.execution.FIX41ExecutionReportBuilder;
import quickfix.examples.fix.builder.execution.FIX42ExecutionReportBuilder;
import quickfix.examples.fix.builder.execution.FIX43ExecutionReportBuilder;
import quickfix.examples.fix.builder.execution.FIX44ExecutionReportBuilder;
import quickfix.examples.fix.builder.execution.FIX50ExecutionReportBuilder;
import quickfix.field.ApplVerID;
import quickfix.field.OrdType;
import quickfix.field.OrderQty;
import quickfix.field.Price;
import quickfix.field.Side;
import quickfix.field.Symbol;

public class Application extends quickfix.MessageCracker implements
		quickfix.Application {
	private static final String DEFAULT_MARKET_PRICE_KEY = "DefaultMarketPrice";
	private static final String ALWAYS_FILL_LIMIT_KEY = "AlwaysFillLimitOrders";
	private static final String VALID_ORDER_TYPES_KEY = "ValidOrderTypes";

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final boolean alwaysFillLimitOrders;
	private final HashSet<String> validOrderTypes = new HashSet<String>();
	private MarketDataProvider marketDataProvider;
	private FIX40ExecutionReportBuilder fix40ExecutionReportBuilder = new FIX40ExecutionReportBuilder();
	private FIX41ExecutionReportBuilder fix41ExecutionReportBuilder = new FIX41ExecutionReportBuilder();
	private FIX42ExecutionReportBuilder fix42ExecutionReportBuilder = new FIX42ExecutionReportBuilder();
	private FIX43ExecutionReportBuilder fix43ExecutionReportBuilder = new FIX43ExecutionReportBuilder();
	private FIX44ExecutionReportBuilder fix44ExecutionReportBuilder = new FIX44ExecutionReportBuilder();
	private FIX50ExecutionReportBuilder fix50ExecutionReportBuilder = new FIX50ExecutionReportBuilder();

	public Application(SessionSettings settings) throws ConfigError,
			FieldConvertError {
		initializeValidOrderTypes(settings);
		initializeMarketDataProvider(settings);

		if (settings.isSetting(ALWAYS_FILL_LIMIT_KEY)) {
			alwaysFillLimitOrders = settings.getBool(ALWAYS_FILL_LIMIT_KEY);
		} else {
			alwaysFillLimitOrders = false;
		}
	}

	private void initializeMarketDataProvider(SessionSettings settings)
			throws ConfigError, FieldConvertError {
		if (settings.isSetting(DEFAULT_MARKET_PRICE_KEY)) {
			if (marketDataProvider == null) {
				final double defaultMarketPrice = settings
						.getDouble(DEFAULT_MARKET_PRICE_KEY);
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

	private void initializeValidOrderTypes(SessionSettings settings)
			throws ConfigError, FieldConvertError {
		if (settings.isSetting(VALID_ORDER_TYPES_KEY)) {
			List<String> orderTypes = Arrays
					.asList(settings.getString(VALID_ORDER_TYPES_KEY).trim()
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

	public void onLogon(SessionID sessionID) {
	}

	public void onLogout(SessionID sessionID) {
	}

	public void toAdmin(quickfix.Message message, SessionID sessionID) {
	}

	public void toApp(quickfix.Message message, SessionID sessionID)
			throws DoNotSend {
	}

	public void fromAdmin(quickfix.Message message, SessionID sessionID)
			throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue,
			RejectLogon {
	}

	public void fromApp(quickfix.Message message, SessionID sessionID)
			throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue,
			UnsupportedMessageType {
		crack(message, sessionID);
	}

	public void onMessage(quickfix.fix40.NewOrderSingle order,
			SessionID sessionID) throws FieldNotFound, UnsupportedMessageType,
			IncorrectTagValue {
		try {
			validateOrder(order);

			OrderQty orderQty = order.getOrderQty();
			Price price = getPrice(order);
			String orderID = genOrderID();

			Message accept = fix40ExecutionReportBuilder.ack(order, orderID,
					genExecID());
			sendMessage(sessionID, accept);

			if (isOrderExecutable(order, price)) {
				Message fill = fix40ExecutionReportBuilder.fill(order, orderID,
						genExecID(), orderQty.getValue(), price.getValue(),
						orderQty.getValue(), price.getValue());

				sendMessage(sessionID, fill);
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
		Price price;
		if (message.getChar(OrdType.FIELD) == OrdType.LIMIT
				&& alwaysFillLimitOrders) {
			price = new Price(message.getDouble(Price.FIELD));
		} else {
			if (marketDataProvider == null) {
				throw new RuntimeException(
						"No market data provider specified for market order");
			}
			char side = message.getChar(Side.FIELD);
			if (side == Side.BUY) {
				price = new Price(marketDataProvider.getAsk(message
						.getString(Symbol.FIELD)));
			} else if (side == Side.SELL || side == Side.SELL_SHORT) {
				price = new Price(marketDataProvider.getBid(message
						.getString(Symbol.FIELD)));
			} else {
				throw new RuntimeException("Invalid order side: " + side);
			}
		}
		return price;
	}

	private void sendMessage(SessionID sessionID, Message message) {
		try {
			Session session = Session.lookupSession(sessionID);
			if (session == null) {
				throw new SessionNotFound(sessionID.toString());
			}

			DataDictionaryProvider dataDictionaryProvider = session
					.getDataDictionaryProvider();
			if (dataDictionaryProvider != null) {
				try {
					dataDictionaryProvider.getApplicationDataDictionary(
							getApplVerID(session, message)).validate(message,
							true);
				} catch (Exception e) {
					LogUtil.logThrowable(
							sessionID,
							"Outgoing message failed validation: "
									+ e.getMessage(), e);
					return;
				}
			}

			session.send(message);
		} catch (SessionNotFound e) {
			log.error(e.getMessage(), e);
		}
	}

	private ApplVerID getApplVerID(Session session, Message message) {
		String beginString = session.getSessionID().getBeginString();
		if (FixVersions.BEGINSTRING_FIXT11.equals(beginString)) {
			return new ApplVerID(ApplVerID.FIX50);
		} else {
			return MessageUtils.toApplVerID(beginString);
		}
	}

	public void onMessage(quickfix.fix41.NewOrderSingle order,
			SessionID sessionID) throws FieldNotFound, UnsupportedMessageType,
			IncorrectTagValue {
		try {
			validateOrder(order);

			OrderQty orderQty = order.getOrderQty();
			Price price = getPrice(order);
			String orderID = genOrderID();

			Message accept = fix41ExecutionReportBuilder.ack(order, orderID,
					genExecID());
			sendMessage(sessionID, accept);

			if (isOrderExecutable(order, price)) {
				Message executionReport = fix41ExecutionReportBuilder
						.fill(order, orderID, genExecID(), orderQty.getValue(),
								price.getValue(), orderQty.getValue(),
								price.getValue());

				sendMessage(sessionID, executionReport);
			}
		} catch (RuntimeException e) {
			LogUtil.logThrowable(sessionID, e.getMessage(), e);
		}
	}

	public void onMessage(quickfix.fix42.NewOrderSingle order,
			SessionID sessionID) throws FieldNotFound, UnsupportedMessageType,
			IncorrectTagValue {
		try {
			validateOrder(order);

			OrderQty orderQty = order.getOrderQty();
			Price price = getPrice(order);
			String orderID = genOrderID();

			Message accept = fix42ExecutionReportBuilder.ack(order, orderID,
					genExecID());
			sendMessage(sessionID, accept);

			if (isOrderExecutable(order, price)) {
				Message executionReport = fix42ExecutionReportBuilder
						.fill(order, orderID, genExecID(), orderQty.getValue(),
								price.getValue(), orderQty.getValue(),
								price.getValue());

				sendMessage(sessionID, executionReport);
			}
		} catch (RuntimeException e) {
			LogUtil.logThrowable(sessionID, e.getMessage(), e);
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

	public void onMessage(quickfix.fix43.NewOrderSingle order,
			SessionID sessionID) throws FieldNotFound, UnsupportedMessageType,
			IncorrectTagValue {
		try {
			validateOrder(order);

			OrderQty orderQty = order.getOrderQty();
			Price price = getPrice(order);
			String orderID = genOrderID();

			Message accept = fix43ExecutionReportBuilder.ack(order, orderID,
					genExecID());
			sendMessage(sessionID, accept);

			if (isOrderExecutable(order, price)) {
				Message executionReport = fix43ExecutionReportBuilder
						.fill(order, orderID, genExecID(), orderQty.getValue(),
								price.getValue(), orderQty.getValue(),
								price.getValue());

				sendMessage(sessionID, executionReport);
			}
		} catch (RuntimeException e) {
			LogUtil.logThrowable(sessionID, e.getMessage(), e);
		}
	}

	public void onMessage(quickfix.fix44.NewOrderSingle order,
			SessionID sessionID) throws FieldNotFound, UnsupportedMessageType,
			IncorrectTagValue {
		try {
			validateOrder(order);

			OrderQty orderQty = order.getOrderQty();
			Price price = getPrice(order);
			String orderID = genOrderID();

			Message accept = fix44ExecutionReportBuilder.ack(order, orderID,
					genExecID());
			sendMessage(sessionID, accept);

			if (isOrderExecutable(order, price)) {
				Message executionReport = fix44ExecutionReportBuilder
						.fill(order, orderID, genExecID(), orderQty.getValue(),
								price.getValue(), orderQty.getValue(),
								price.getValue());

				sendMessage(sessionID, executionReport);
			}
		} catch (RuntimeException e) {
			LogUtil.logThrowable(sessionID, e.getMessage(), e);
		}
	}

	public void onMessage(quickfix.fix50.NewOrderSingle order,
			SessionID sessionID) throws FieldNotFound, UnsupportedMessageType,
			IncorrectTagValue {
		try {
			validateOrder(order);

			OrderQty orderQty = order.getOrderQty();
			Price price = getPrice(order);
			String orderID = genOrderID();

			Message accept = fix50ExecutionReportBuilder.ack(order, orderID,
					genExecID());
			sendMessage(sessionID, accept);

			if (isOrderExecutable(order, price)) {
				Message executionReport = fix50ExecutionReportBuilder
						.fill(order, orderID, genExecID(), orderQty.getValue(),
								price.getValue(), orderQty.getValue(),
								price.getValue());

				sendMessage(sessionID, executionReport);
			}
		} catch (RuntimeException e) {
			LogUtil.logThrowable(sessionID, e.getMessage(), e);
		}
	}

	public String genOrderID() {
		return Integer.valueOf(++m_orderID).toString();
	}

	public String genExecID() {
		return Integer.valueOf(++m_execID).toString();
	}

	/**
	 * Allows a custom market data provider to be specified.
	 * 
	 * @param marketDataProvider
	 */
	public void setMarketDataProvider(MarketDataProvider marketDataProvider) {
		this.marketDataProvider = marketDataProvider;
	}

	private int m_orderID = 0;
	private int m_execID = 0;
}
