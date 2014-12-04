package quickfix.examples.banzai.fixformat;

import static quickfix.examples.banzai.TypeMapping.*;
import quickfix.examples.banzai.Order;
import quickfix.field.ClOrdID;
import quickfix.field.HandlInst;
import quickfix.field.OrderQty;
import quickfix.field.Symbol;
import quickfix.field.TransactTime;

public class Fix50MessageBuilder implements FixMessageBuilder {

	public quickfix.Message send(Order order) {
        quickfix.fix50.NewOrderSingle newOrderSingle = new quickfix.fix50.NewOrderSingle(
                new ClOrdID(order.getID()), sideToFIXSide(order.getSide()),
                new TransactTime(), typeToFIXType(order.getType()));
        newOrderSingle.set(new OrderQty(order.getQuantity()));
        newOrderSingle.set(new Symbol(order.getSymbol()));
        newOrderSingle.set(new HandlInst('1'));

		return newOrderSingle;
	}

	public quickfix.Message replace(Order order, Order newOrder) {
		throw new UnsupportedOperationException();
	}

	public quickfix.Message cancel(Order order) {
		throw new UnsupportedOperationException();
	}

}
