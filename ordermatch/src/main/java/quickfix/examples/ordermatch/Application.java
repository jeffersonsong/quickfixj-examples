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

package quickfix.examples.ordermatch;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quickfix.DoNotSend;
import quickfix.FieldNotFound;
import quickfix.IncorrectDataFormat;
import quickfix.IncorrectTagValue;
import quickfix.Message;
import quickfix.MessageCracker;
import quickfix.RejectLogon;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionNotFound;
import quickfix.UnsupportedMessageType;
import quickfix.examples.fix.builder.execution.ExecutionReportBuilder;
import quickfix.examples.fix.builder.execution.FIX42ExecutionReportBuilder;
import quickfix.field.NoRelatedSym;
import quickfix.field.OrdStatus;
import quickfix.field.OrdType;
import quickfix.field.OrderQty;
import quickfix.field.OrigClOrdID;
import quickfix.field.Price;
import quickfix.field.Side;
import quickfix.field.SubscriptionRequestType;
import quickfix.field.Symbol;
import quickfix.field.TimeInForce;
import quickfix.fix42.MarketDataRequest;

public class Application extends MessageCracker implements quickfix.Application {
	private static final Logger log = LoggerFactory
			.getLogger(Application.class);

	private OrderMatcher orderMatcher = new OrderMatcher();
	private IdGenerator generator = new IdGenerator();
	private final MessageSender messageSender;
	private ExecutionReportBuilder fix42Builder = new FIX42ExecutionReportBuilder();

	public Application() {
		this(new MessageSender() {
			public boolean sendToTarget(Message message) {
				try {
					return Session.sendToTarget(message);
				} catch (SessionNotFound e) {
					log.error("Failed to send: " + message, e);
					return false;
				}
			}
		});
	}

	public Application(MessageSender messageSender) {
		this.messageSender = messageSender;
	}

	public void fromAdmin(Message message, SessionID sessionId)
			throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue,
			RejectLogon {
	}

	public void fromApp(Message message, SessionID sessionId)
			throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue,
			UnsupportedMessageType {
		crack(message, sessionId);
	}

	public void onMessage(quickfix.fix42.NewOrderSingle message,
			SessionID sessionID) throws FieldNotFound, UnsupportedMessageType,
			IncorrectTagValue {
		char timeInForce = TimeInForce.DAY;
		if (message.isSetField(TimeInForce.FIELD)) {
			timeInForce = message.getChar(TimeInForce.FIELD);
			if (timeInForce != TimeInForce.DAY) {
				rejectOrder(message, "Unsupported TIF, use Day");
				return;
			}
		}

		try {
			Order order = new Order(generator.genOrderID(), message);
			processOrder(order);
		} catch (Exception e) {
			rejectOrder(message, e.getMessage());
		}
	}

	private void processOrder(Order order) throws FieldNotFound {
		if (orderMatcher.insert(order)) {
			acceptOrder(order);

			ArrayList<Order> orders = new ArrayList<Order>();
			orderMatcher.match(order.getSymbol(), orders);

			while (orders.size() > 0) {
				fillOrder(orders.remove(0));
			}
			orderMatcher.display(order.getSymbol());
		} else {
			rejectOrder(order);
		}
	}

	private void rejectOrder(Message request, String message)
			throws FieldNotFound {
		Message execRpt = fix42Builder.reject(request,
				generator.genExecutionID(), generator.genOrderID(), message);

		send(execRpt);
	}

	private void rejectOrder(Order order) throws FieldNotFound {
		Message execRpt = fix42Builder.reject(order.getMessage(),
				generator.genExecutionID(), generator.genOrderID(), "");
		send(execRpt);
	}

	private void acceptOrder(Order order) throws FieldNotFound {
		Message execRpt = fix42Builder.ack(order.getMessage(),
				order.getOrderID(), generator.genExecutionID());
		send(execRpt);
	}

	private void fillOrder(Order order) throws FieldNotFound {
		char ordStatus = order.getExecutedQuantity() == order.getQuantity() ? OrdStatus.FILLED
				: OrdStatus.PARTIALLY_FILLED;
		Message execRpt = fix42Builder.fill(order.getMessage(),
				order.getOrderID(), generator.genExecutionID(), ordStatus,
				order.getExecutedQuantity(), order.getAvgExecutedPrice(),
				order.getLastExecutedQuantity(), order.getLastExecutedPrice());
		send(execRpt);
	}

	public void onMessage(quickfix.fix42.OrderCancelRequest message,
			SessionID sessionID) throws FieldNotFound {
		String symbol = message.getString(Symbol.FIELD);
		char side = message.getChar(Side.FIELD);
		String id = message.getString(OrigClOrdID.FIELD);
		Order order = orderMatcher.find(symbol, side, id);
		order.cancel();
		cancelOrder(order, message);
		orderMatcher.erase(order);
	}

	private void cancelOrder(Order order, Message message) throws FieldNotFound {
		Message execRpt = fix42Builder.canceled(message, order.getOrderID(),
				generator.genExecutionID(), order.getExecutedQuantity(),
				order.getAvgExecutedPrice());
		send(execRpt);
	}

	public void onMessage(quickfix.fix42.OrderCancelReplaceRequest message,
			SessionID sessionID) throws FieldNotFound {
		String symbol = message.getString(Symbol.FIELD);
		char side = message.getChar(Side.FIELD);
		String id = message.getString(OrigClOrdID.FIELD);
		Order order = orderMatcher.find(symbol, side, id);

		long newQty = (long) message.getDouble(OrderQty.FIELD);
		char newType = message.getChar(OrdType.FIELD);
		double newPrice = 0;
		if (message.isSetField(Price.FIELD)) {
			newPrice = message.getDouble(Price.FIELD);
		}

		try {
			order.replace(newQty, newType, newPrice);
			replaceOrder(order, message);
		} catch (Exception ex) {
			log.error("Failed to process: " + message, ex);
		}
	}

	private void replaceOrder(Order order, Message message)
			throws FieldNotFound {
		Message execRpt = fix42Builder.replaced(message, order.getOrderID(),
				generator.genExecutionID(), order.getExecutedQuantity(),
				order.getAvgExecutedPrice());
		send(execRpt);
	}

	public void onMessage(MarketDataRequest message, SessionID sessionID)
			throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue {
		MarketDataRequest.NoRelatedSym noRelatedSyms = new MarketDataRequest.NoRelatedSym();

		// String mdReqId = message.getString(MDReqID.FIELD);
		char subscriptionRequestType = message
				.getChar(SubscriptionRequestType.FIELD);

		if (subscriptionRequestType != SubscriptionRequestType.SNAPSHOT)
			throw new IncorrectTagValue(SubscriptionRequestType.FIELD);
		// int marketDepth = message.getInt(MarketDepth.FIELD);
		int relatedSymbolCount = message.getInt(NoRelatedSym.FIELD);

		for (int i = 1; i <= relatedSymbolCount; ++i) {
			message.getGroup(i, noRelatedSyms);
			String symbol = noRelatedSyms.getString(Symbol.FIELD);
			log.error("*** market data: " + symbol);
		}
	}

	public void onCreate(SessionID sessionId) {
	}

	public void onLogon(SessionID sessionId) {
		log.info("Logon - " + sessionId);
	}

	public void onLogout(SessionID sessionId) {
		log.info("Logout - " + sessionId);
	}

	public void toAdmin(Message message, SessionID sessionId) {
		// empty
	}

	public void toApp(Message message, SessionID sessionId) throws DoNotSend {
		// empty
	}

	public OrderMatcher orderMatcher() {
		return orderMatcher;
	}

	private void send(Message message) {
		messageSender.sendToTarget(message);
	}
}