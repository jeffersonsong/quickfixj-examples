package quickfix.examples.banzai.fixformat;

import static quickfix.examples.banzai.TypeMapping.*;
import quickfix.examples.banzai.Order;
import quickfix.field.ClOrdID;
import quickfix.field.HandlInst;
import quickfix.field.OrderQty;
import quickfix.field.OrigClOrdID;
import quickfix.field.Symbol;

public class Fix41MessageBuilder implements FixMessageBuilder {

	public quickfix.Message send(Order order) {
        quickfix.fix41.NewOrderSingle newOrderSingle = new quickfix.fix41.NewOrderSingle(
                new ClOrdID(order.getID()), new HandlInst('1'), new Symbol(order.getSymbol()),
                sideToFIXSide(order.getSide()), typeToFIXType(order.getType()));
        newOrderSingle.set(new OrderQty(order.getQuantity()));

		return newOrderSingle;
	}

	public quickfix.Message replace(Order order, Order newOrder) {
        quickfix.fix41.OrderCancelReplaceRequest message = new quickfix.fix41.OrderCancelReplaceRequest(
                new OrigClOrdID(order.getID()), new ClOrdID(newOrder.getID()), new HandlInst('1'),
                new Symbol(order.getSymbol()), sideToFIXSide(order.getSide()), typeToFIXType(order
                        .getType()));

		return message;
	}

	public quickfix.Message cancel(Order order) {
        String id = order.generateID();
        quickfix.fix41.OrderCancelRequest message = new quickfix.fix41.OrderCancelRequest(
                new OrigClOrdID(order.getID()), new ClOrdID(id), new Symbol(order.getSymbol()),
                sideToFIXSide(order.getSide()));
        message.setField(new OrderQty(order.getQuantity()));

		return message;
	}

}
