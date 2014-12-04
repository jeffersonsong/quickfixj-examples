package quickfix.examples.banzai.fixformat;

import static quickfix.examples.banzai.TypeMapping.*;
import quickfix.examples.banzai.Order;
import quickfix.field.ClOrdID;
import quickfix.field.HandlInst;
import quickfix.field.OrderQty;
import quickfix.field.OrigClOrdID;
import quickfix.field.Symbol;
import quickfix.field.TransactTime;

public class Fix42MessageBuilder implements FixMessageBuilder {

	public quickfix.Message send(Order order) {
        quickfix.fix42.NewOrderSingle newOrderSingle = new quickfix.fix42.NewOrderSingle(
                new ClOrdID(order.getID()), new HandlInst('1'), new Symbol(order.getSymbol()),
                sideToFIXSide(order.getSide()), new TransactTime(), typeToFIXType(order.getType()));
        newOrderSingle.set(new OrderQty(order.getQuantity()));

		return newOrderSingle;
	}

	public quickfix.Message replace(Order order, Order newOrder) {
        quickfix.fix42.OrderCancelReplaceRequest message = new quickfix.fix42.OrderCancelReplaceRequest(
                new OrigClOrdID(order.getID()), new ClOrdID(newOrder.getID()), new HandlInst('1'),
                new Symbol(order.getSymbol()), sideToFIXSide(order.getSide()), new TransactTime(),
                typeToFIXType(order.getType()));

		return message;
	}

	public quickfix.Message cancel(Order order) {
        String id = order.generateID();
        quickfix.fix42.OrderCancelRequest message = new quickfix.fix42.OrderCancelRequest(
                new OrigClOrdID(order.getID()), new ClOrdID(id), new Symbol(order.getSymbol()),
                sideToFIXSide(order.getSide()), new TransactTime());
        message.setField(new OrderQty(order.getQuantity()));

		return message;
	}

}
