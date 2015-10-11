package quickfix.examples.banzai.fix;

import static quickfix.examples.banzai.model.TypeMapping.sideToFIXSide;
import static quickfix.examples.banzai.model.TypeMapping.typeToFIXType;
import quickfix.Message;
import quickfix.MessageFactory;
import quickfix.examples.banzai.model.Order;
import quickfix.field.ClOrdID;
import quickfix.field.HandlInst;
import quickfix.field.OrderQty;
import quickfix.field.OrigClOrdID;
import quickfix.field.Symbol;
import quickfix.field.TransactTime;
import quickfix.fix44.OrderCancelReplaceRequest;
import quickfix.fix44.OrderCancelRequest;
import quickfix.fix44.NewOrderSingle;

public class Fix44MessageBuilder extends AbstractFixMessageBuilder {

	public Fix44MessageBuilder(MessageFactory messageFactory) {
		super(messageFactory);
	}

	public Message createNewOrderSingle(Order order) {
		NewOrderSingle newOrderSingle = new NewOrderSingle(new ClOrdID(
				order.getID()), sideToFIXSide(order.getSide()),
				new TransactTime(), typeToFIXType(order.getType()));
		newOrderSingle.set(new OrderQty(order.getQuantity()));
		newOrderSingle.set(new Symbol(order.getSymbol()));
		newOrderSingle.set(new HandlInst('1'));

		return newOrderSingle;
	}

	public Message createReplaceRequest(Order order, Order newOrder) {
		OrderCancelReplaceRequest message = new OrderCancelReplaceRequest(
				new OrigClOrdID(order.getID()), new ClOrdID(newOrder.getID()),
				sideToFIXSide(order.getSide()), new TransactTime(),
				typeToFIXType(order.getType()));

		return message;
	}

	public Message createCancelRequest(Order order) {
		String id = order.generateID();
		OrderCancelRequest message = new OrderCancelRequest(new OrigClOrdID(
				order.getID()), new ClOrdID(id),
				sideToFIXSide(order.getSide()), new TransactTime());
		message.setField(new OrderQty(order.getQuantity()));

		return message;
	}
}
