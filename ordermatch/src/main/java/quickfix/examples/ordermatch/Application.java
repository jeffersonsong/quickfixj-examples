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

import static quickfix.examples.ordermatch.FixMessageHelper.reject;

import java.util.ArrayList;

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
import quickfix.examples.utility.MessageSender;
import quickfix.field.ClOrdID;
import quickfix.field.NoRelatedSym;
import quickfix.field.OrdStatus;
import quickfix.field.OrigClOrdID;
import quickfix.field.Side;
import quickfix.field.SubscriptionRequestType;
import quickfix.field.Symbol;
import quickfix.field.TimeInForce;
import quickfix.fix42.ExecutionReport;
import quickfix.fix42.MarketDataRequest;
import quickfix.fix42.NewOrderSingle;
import quickfix.fix42.OrderCancelRequest;

public class Application extends MessageCracker implements quickfix.Application {
	private OrderMatcher orderMatcher = new OrderMatcher();
	private IdGenerator generator = new IdGenerator();
	private final MessageSender messageSender;

	public Application() {
		this(new MessageSender() {
			public boolean sendToTarget(Message message) throws SessionNotFound {
				try {
					return Session.sendToTarget(message);
				} catch (SessionNotFound e) {
					e.printStackTrace();
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

	public void onMessage(NewOrderSingle message, SessionID sessionID)
			throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue {
		char timeInForce = TimeInForce.DAY;
		if (message.isSetField(TimeInForce.FIELD)) {
			timeInForce = message.getChar(TimeInForce.FIELD);
		}

		try {
			if (timeInForce != TimeInForce.DAY) {
				throw new RuntimeException("Unsupported TIF, use Day");
			}

			Order order = new Order(generator.genOrderID(), message);

			processOrder(order);
		} catch (Exception e) {
			rejectOrder(message, e.getMessage());
		}
	}

	private void rejectOrder(NewOrderSingle request, String message)
			throws FieldNotFound {
		String clOrdId = request.getString(ClOrdID.FIELD);
		ExecutionReport fixOrder = reject(generator.genExecutionID(),
				clOrdId, request, message);

		try {
			messageSender.sendToTarget(fixOrder);
		} catch (SessionNotFound e) {
			e.printStackTrace();
		}
	}

	private void processOrder(Order order) {
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

	private void rejectOrder(Order order) {
		updateOrder(order, OrdStatus.REJECTED, (NewOrderSingle)order.getMessage());
	}

	private void acceptOrder(Order order) {
		updateOrder(order, OrdStatus.NEW, (NewOrderSingle)order.getMessage());
	}

	private void cancelOrder(Order order, OrderCancelRequest message) {
		updateOrder(order, OrdStatus.CANCELED, message);
	}

	private void updateOrder(Order order, char status, NewOrderSingle request) {
		try {
			ExecutionReport fixOrder = FixMessageHelper.updateOrder(
					generator.genExecutionID(), order.getOrderID(), order,
					status, request);
			messageSender.sendToTarget(fixOrder);
		} catch (SessionNotFound e) {
		} catch (FieldNotFound e) {
		}
	}
	
	private void updateOrder(Order order, char status, OrderCancelRequest request) {
		try {
			ExecutionReport fixOrder = FixMessageHelper.updateOrder(
					generator.genExecutionID(), order.getOrderID(), order,
					status, request);
			messageSender.sendToTarget(fixOrder);
		} catch (SessionNotFound e) {
		} catch (FieldNotFound e) {
		}
	}

	private void fillOrder(Order order) {
		updateOrder(order, order.isFilled() ? OrdStatus.FILLED
				: OrdStatus.PARTIALLY_FILLED, (NewOrderSingle)order.getMessage());
	}

	public void onMessage(OrderCancelRequest message, SessionID sessionID)
			throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue {
		String symbol = message.getString(Symbol.FIELD);
		char side = message.getChar(Side.FIELD);
		String id = message.getString(OrigClOrdID.FIELD);
		Order order = orderMatcher.find(symbol, side, id);
		order.cancel();
		cancelOrder(order, message);
		orderMatcher.erase(order);
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
			System.err.println("*** market data: " + symbol);
		}
	}

	public void onCreate(SessionID sessionId) {
	}

	public void onLogon(SessionID sessionId) {
		System.out.println("Logon - " + sessionId);
	}

	public void onLogout(SessionID sessionId) {
		System.out.println("Logout - " + sessionId);
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
}