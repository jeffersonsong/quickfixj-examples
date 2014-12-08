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

package quickfix.examples.banzai;

import static quickfix.examples.banzai.model.TypeMapping.FIXSideToSide;
import static quickfix.examples.banzai.model.TypeMapping.FIXTypeToType;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;

import javax.swing.SwingUtilities;

import quickfix.ApplicationAdapter;
import quickfix.DefaultMessageFactory;
import quickfix.FieldNotFound;
import quickfix.IncorrectDataFormat;
import quickfix.IncorrectTagValue;
import quickfix.Message;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionNotFound;
import quickfix.UnsupportedMessageType;
import quickfix.examples.banzai.fix.FixMessageBuilder;
import quickfix.examples.banzai.fix.FixMessageBuilderFactory;
import quickfix.examples.banzai.model.Execution;
import quickfix.examples.banzai.model.LogonEvent;
import quickfix.examples.banzai.model.Order;
import quickfix.examples.utility.DefaultMessageSender;
import quickfix.examples.utility.MessageSender;
import quickfix.field.AvgPx;
import quickfix.field.BeginString;
import quickfix.field.BusinessRejectReason;
import quickfix.field.ClOrdID;
import quickfix.field.CumQty;
import quickfix.field.DeliverToCompID;
import quickfix.field.ExecID;
import quickfix.field.LastPx;
import quickfix.field.LastShares;
import quickfix.field.LeavesQty;
import quickfix.field.MsgType;
import quickfix.field.OrdStatus;
import quickfix.field.OrdType;
import quickfix.field.OrderQty;
import quickfix.field.OrigClOrdID;
import quickfix.field.Price;
import quickfix.field.SessionRejectReason;
import quickfix.field.Side;
import quickfix.field.Symbol;
import quickfix.field.Text;

public class BanzaiApplication extends ApplicationAdapter {

	private OrderTableModel orderTableModel = null;
	private ExecutionTableModel executionTableModel = null;
	private ObservableOrder observableOrder = new ObservableOrder();
	private ObservableLogon observableLogon = new ObservableLogon();
	private boolean isAvailable = true;
	private boolean isMissingField;
	private MessageSender messageSender = new DefaultMessageSender();

	static private HashMap<SessionID, HashSet<ExecID>> execIDs = new HashMap<SessionID, HashSet<ExecID>>();

	static private FixMessageBuilderFactory fixMessageBuilderFactory = new FixMessageBuilderFactory(new DefaultMessageFactory());

	public BanzaiApplication(OrderTableModel orderTableModel,
			ExecutionTableModel executionTableModel) {
		this.orderTableModel = orderTableModel;
		this.executionTableModel = executionTableModel;
	}

	public void onLogon(SessionID sessionID) {
		observableLogon.logon(sessionID);
	}

	public void onLogout(SessionID sessionID) {
		observableLogon.logoff(sessionID);
	}

	public void fromApp(quickfix.Message message, SessionID sessionID)
			throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue,
			UnsupportedMessageType {
		try {
			SwingUtilities
					.invokeLater(new MessageProcessor(message, sessionID));
		} catch (Exception e) {
		}
	}

	public class MessageProcessor implements Runnable {
		private quickfix.Message message;
		private SessionID sessionID;

		public MessageProcessor(quickfix.Message message, SessionID sessionID) {
			this.message = message;
			this.sessionID = sessionID;
		}

		public void run() {
			try {
				MsgType msgType = new MsgType();
				if (isAvailable) {
					if (isMissingField) {
						// For OpenFIX certification testing
						sendBusinessReject(
								message,
								BusinessRejectReason.CONDITIONALLY_REQUIRED_FIELD_MISSING,
								"Conditionally required field missing");
					} else if (message.getHeader().isSetField(
							DeliverToCompID.FIELD)) {
						// This is here to support OpenFIX certification
						sendSessionReject(message,
								SessionRejectReason.COMPID_PROBLEM);
					} else if (message.getHeader().getField(msgType)
							.valueEquals("8")) {
						executionReport(message, sessionID);
					} else if (message.getHeader().getField(msgType)
							.valueEquals("9")) {
						cancelReject(message, sessionID);
					} else {
						sendBusinessReject(message,
								BusinessRejectReason.UNSUPPORTED_MESSAGE_TYPE,
								"Unsupported Message Type");
					}
				} else {
					sendBusinessReject(message,
							BusinessRejectReason.APPLICATION_NOT_AVAILABLE,
							"Application not available");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	private void sendSessionReject(Message message, int rejectReason)
			throws FieldNotFound, SessionNotFound {
		String beginString = message.getHeader().getString(BeginString.FIELD);
		FixMessageBuilder builder = getFixMessageBuilder(beginString);
		Message reply = builder.sessionReject(message, rejectReason);
		Session.sendToTarget(reply);
	}

	private void sendBusinessReject(Message message, int rejectReason,
			String rejectText) throws FieldNotFound, SessionNotFound {
		String beginString = message.getHeader().getString(BeginString.FIELD);
		FixMessageBuilder builder = getFixMessageBuilder(beginString);
		Message reply = builder.businessReject(message, rejectReason,
				rejectText);
		Session.sendToTarget(reply);
	}

	private void executionReport(Message message, SessionID sessionID)
			throws FieldNotFound {

		ExecID execID = (ExecID) message.getField(new ExecID());
		if (alreadyProcessed(execID, sessionID))
			return;

		Order order = orderTableModel.getOrder(message.getField(new ClOrdID())
				.getValue());
		if (order == null) {
			return;
		}

		BigDecimal fillSize = BigDecimal.ZERO;

		if (message.isSetField(LastShares.FIELD)) {
			LastShares lastShares = new LastShares();
			message.getField(lastShares);
			fillSize = new BigDecimal("" + lastShares.getValue());
		} else {
			// > FIX 4.1
			LeavesQty leavesQty = new LeavesQty();
			message.getField(leavesQty);
			fillSize = new BigDecimal(order.getQuantity())
					.subtract(new BigDecimal("" + leavesQty.getValue()));
		}

		if (fillSize.compareTo(BigDecimal.ZERO) > 0) {
			order.setOpen(order.getOpen()
					- (int) Double.parseDouble(fillSize.toPlainString()));
			order.setExecuted(new Integer(message.getString(CumQty.FIELD)));
			order.setAvgPx(new Double(message.getString(AvgPx.FIELD)));
		}

		OrdStatus ordStatus = (OrdStatus) message.getField(new OrdStatus());

		if (ordStatus.valueEquals(OrdStatus.REJECTED)) {
			order.setRejected(true);
			order.setOpen(0);
		} else if (ordStatus.valueEquals(OrdStatus.CANCELED)
				|| ordStatus.valueEquals(OrdStatus.DONE_FOR_DAY)) {
			order.setCanceled(true);
			order.setOpen(0);
		} else if (ordStatus.valueEquals(OrdStatus.NEW)) {
			if (order.isNew()) {
				order.setNew(false);
			}
		} else if (ordStatus.valueEquals(OrdStatus.REPLACED)) {
			order.setQuantity((int)message.getDouble(OrderQty.FIELD));
			order.setOpen((int)message.getDouble(LeavesQty.FIELD));
			OrdType ordType = new OrdType();
			message.getField(ordType);
			order.setType(FIXTypeToType(ordType));
			if (message.isSetField(Price.FIELD)) {
				order.setLimit(message.getDouble(Price.FIELD));
			}
		}

		try {
			order.setMessage(message.getField(new Text()).getValue());
		} catch (FieldNotFound e) {
		}

		orderTableModel.updateOrder(order, message.getField(new ClOrdID())
				.getValue());
		observableOrder.update(order);

		if (fillSize.compareTo(BigDecimal.ZERO) > 0) {
			Execution execution = new Execution();
			execution.setExchangeID(sessionID
					+ message.getField(new ExecID()).getValue());

			execution.setSymbol(message.getField(new Symbol()).getValue());
			execution.setQuantity(fillSize.intValue());
			if (message.isSetField(LastPx.FIELD)) {
				execution.setPrice(new Double(message.getString(LastPx.FIELD)));
			}
			Side side = (Side) message.getField(new Side());
			execution.setSide(FIXSideToSide(side));
			executionTableModel.addExecution(execution);
		}
	}

	private void cancelReject(Message message, SessionID sessionID)
			throws FieldNotFound {
		String id = message.getField(new ClOrdID()).getValue();
		Order order = orderTableModel.getOrder(id);
		if (order == null)
			return;
		if (order.getOriginalID() != null)
			order = orderTableModel.getOrder(order.getOriginalID());

		try {
			order.setMessage(message.getField(new Text()).getValue());
		} catch (FieldNotFound e) {
		}
		orderTableModel.updateOrder(order, message.getField(new OrigClOrdID())
				.getValue());
	}

	private boolean alreadyProcessed(ExecID execID, SessionID sessionID) {
		HashSet<ExecID> set = execIDs.get(sessionID);
		if (set == null) {
			set = new HashSet<ExecID>();
			set.add(execID);
			execIDs.put(sessionID, set);
			return false;
		} else {
			if (set.contains(execID))
				return true;
			set.add(execID);
			return false;
		}
	}


	public void send(Order order) {
		String beginString = order.getSessionID().getBeginString();
		FixMessageBuilder builder = getFixMessageBuilder(beginString);
		quickfix.Message newOrderSingle = builder.newOrder(order);
		send(newOrderSingle, order.getSessionID());
		return;
	}

	public void cancel(Order order) {
		String beginString = order.getSessionID().getBeginString();
		FixMessageBuilder builder = getFixMessageBuilder(beginString);
		quickfix.Message message = builder.cancel(order);
		orderTableModel.addID(order, order.getID());
		send(message, order.getSessionID());
		return;
	}

	public void replace(Order order, Order newOrder) {
		String beginString = order.getSessionID().getBeginString();
		FixMessageBuilder builder = getFixMessageBuilder(beginString);
		quickfix.Message message = builder.replace(order, newOrder);
		orderTableModel.addID(order, newOrder.getID());
		send(message, order.getSessionID());
		return;
	}
	
	private void send(quickfix.Message message, SessionID sessionID) {
		messageSender.sendMessage(message, sessionID);
	}

	public void addLogonObserver(Observer observer) {
		observableLogon.addObserver(observer);
	}

	public void deleteLogonObserver(Observer observer) {
		observableLogon.deleteObserver(observer);
	}

	public void addOrderObserver(Observer observer) {
		observableOrder.addObserver(observer);
	}

	public void deleteOrderObserver(Observer observer) {
		observableOrder.deleteObserver(observer);
	}

	private static class ObservableOrder extends Observable {
		public void update(Order order) {
			setChanged();
			notifyObservers(order);
			clearChanged();
		}
	}

	private static class ObservableLogon extends Observable {
		private HashSet<SessionID> set = new HashSet<SessionID>();

		public void logon(SessionID sessionID) {
			set.add(sessionID);
			setChanged();
			notifyObservers(new LogonEvent(sessionID, true));
			clearChanged();
		}

		public void logoff(SessionID sessionID) {
			set.remove(sessionID);
			setChanged();
			notifyObservers(new LogonEvent(sessionID, false));
			clearChanged();
		}
	}

	public boolean isMissingField() {
		return isMissingField;
	}

	public void setMissingField(boolean isMissingField) {
		this.isMissingField = isMissingField;
	}

	public boolean isAvailable() {
		return isAvailable;
	}

	public void setAvailable(boolean isAvailable) {
		this.isAvailable = isAvailable;
	}

	private FixMessageBuilder getFixMessageBuilder(String beginString) {
		return fixMessageBuilderFactory.getFixMessageBuilder(beginString);
	}
}
