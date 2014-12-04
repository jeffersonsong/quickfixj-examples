package quickfix.examples.banzai.fixformat;

import static quickfix.examples.banzai.TypeMapping.*;
import quickfix.examples.banzai.Order;
import quickfix.field.ClOrdID;
import quickfix.field.CxlType;
import quickfix.field.HandlInst;
import quickfix.field.OrderQty;
import quickfix.field.OrigClOrdID;
import quickfix.field.Symbol;

public class Fix40MessageBuilder implements FixMessageBuilder {

	public quickfix.Message send(Order order) {
		quickfix.fix40.NewOrderSingle newOrderSingle = new quickfix.fix40.NewOrderSingle(
				new ClOrdID(order.getID()), new HandlInst('1'), new Symbol(
						order.getSymbol()), sideToFIXSide(order.getSide()),
				new OrderQty(order.getQuantity()),
				typeToFIXType(order.getType()));

		return newOrderSingle;
	}

	public quickfix.Message replace(Order order, Order newOrder) {
		quickfix.fix40.OrderCancelReplaceRequest message = new quickfix.fix40.OrderCancelReplaceRequest(
				new OrigClOrdID(order.getID()), new ClOrdID(newOrder.getID()),
				new HandlInst('1'), new Symbol(order.getSymbol()),
				sideToFIXSide(order.getSide()), new OrderQty(
						newOrder.getQuantity()), typeToFIXType(order.getType()));

		return message;
	}

	public quickfix.Message cancel(Order order) {
		String id = order.generateID();
		quickfix.fix40.OrderCancelRequest message = new quickfix.fix40.OrderCancelRequest(
				new OrigClOrdID(order.getID()), new ClOrdID(id), new CxlType(
						CxlType.FULL_REMAINING_QUANTITY), new Symbol(
						order.getSymbol()), sideToFIXSide(order.getSide()),
				new OrderQty(order.getQuantity()));

		return message;
	}

}
