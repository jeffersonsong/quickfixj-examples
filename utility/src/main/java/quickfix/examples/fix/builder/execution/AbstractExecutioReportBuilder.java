package quickfix.examples.fix.builder.execution;

import quickfix.FieldNotFound;
import quickfix.Message;
import quickfix.field.SenderCompID;
import quickfix.field.TargetCompID;

public abstract class AbstractExecutioReportBuilder implements
		ExecutionReportBuilder {

	protected static void reverseRoute(Message message, Message reply)
			throws FieldNotFound {
		reply.getHeader().setString(SenderCompID.FIELD,
				message.getHeader().getString(TargetCompID.FIELD));
		reply.getHeader().setString(TargetCompID.FIELD,
				message.getHeader().getString(SenderCompID.FIELD));
	}

	public Message ack(Message newOrderSingle, String orderID, String execID)
			throws FieldNotFound {
		throw new UnsupportedOperationException();
	}

	public Message reject(Message newOrderSingle, String orderID,
			String execID, String text) throws FieldNotFound {
		throw new UnsupportedOperationException();
	}

	public Message fill(Message newOrderSingle, String orderID, String execID,
			char ordStatus, double cumQty, double avgPx, double lastShares,
			double lastPx) throws FieldNotFound {
		throw new UnsupportedOperationException();
	}

	public Message canceled(Message cancelRequest, String orderID,
			String execID, double cumQty, double avgPx) throws FieldNotFound {
		throw new UnsupportedOperationException();
	}

	public Message replaced(Message replaceRequest, String orderID,
			String execID, double cumQty, double avgPx) throws FieldNotFound {
		throw new UnsupportedOperationException();
	}
}
