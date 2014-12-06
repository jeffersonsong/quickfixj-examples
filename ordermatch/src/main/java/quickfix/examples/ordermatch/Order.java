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

import quickfix.FieldNotFound;
import quickfix.Message;
import quickfix.field.ClOrdID;
import quickfix.field.OrdType;
import quickfix.field.OrderQty;
import quickfix.field.Price;
import quickfix.field.SenderCompID;
import quickfix.field.Side;
import quickfix.field.Symbol;
import quickfix.fix42.NewOrderSingle;

public class Order {
	private long entryTime;
	private String orderID;
	private String clOrdID;
	private String symbol;
	private String owner;
	private char side;
	private char type;
	private double price;
	private long quantity;
	private long openQuantity;
	private long executedQuantity;
	private double avgExecutedPrice;
	private double lastExecutedPrice;
	private long lastExecutedQuantity;
	private Message message;

	public Order(String orderID, NewOrderSingle message) throws FieldNotFound {
		super();

		this.orderID = orderID;
		this.message = message;
		
		this.clOrdID = message.getString(ClOrdID.FIELD);

		this.symbol = message.getString(Symbol.FIELD);
		this.owner = message.getHeader().getString(SenderCompID.FIELD);
		this.side = message.getChar(Side.FIELD);
		this.type = message.getChar(OrdType.FIELD);
		this.price = 0;
		if (this.type == OrdType.LIMIT) {
			this.price = message.getDouble(Price.FIELD);
		}
		this.quantity = (int) message.getDouble(OrderQty.FIELD);
		this.openQuantity = quantity;
		this.entryTime = System.currentTimeMillis();
	}

	public double getAvgExecutedPrice() {
		return avgExecutedPrice;
	}

	public String getOrderID() {
		return orderID;
	}
	
	public String getClOrdID() {
		return this.clOrdID;
	}

	public long getExecutedQuantity() {
		return executedQuantity;
	}

	public long getLastExecutedQuantity() {
		return lastExecutedQuantity;
	}

	public long getOpenQuantity() {
		return openQuantity;
	}

	public String getOwner() {
		return owner;
	}

	public double getPrice() {
		return price;
	}

	public long getQuantity() {
		return quantity;
	}

	public char getSide() {
		return side;
	}

	public String getSymbol() {
		return symbol;
	}

	public char getType() {
		return type;
	}

	public boolean isFilled() {
		return quantity == executedQuantity;
	}

	public void cancel() {
		openQuantity = 0;
	}

	public boolean isClosed() {
		return openQuantity == 0;
	}

	public void execute(double price, long quantity) {
		avgExecutedPrice = ((quantity * price) + (avgExecutedPrice * executedQuantity))
				/ (quantity + executedQuantity);

		openQuantity -= quantity;
		executedQuantity += quantity;
		lastExecutedPrice = price;
		lastExecutedQuantity = quantity;
	}

	public Message getMessage() {
		return message;
	}

	public String toString() {
		return (side == Side.BUY ? "BUY" : "SELL") + " " + quantity + "@$"
				+ price + " (" + openQuantity + ")";
	}

	public long getEntryTime() {
		return entryTime;
	}

	public double getLastExecutedPrice() {
		return lastExecutedPrice;
	}
}