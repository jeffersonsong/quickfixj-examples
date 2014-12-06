package quickfix.examples.fix.builder.execution;

import quickfix.FieldNotFound;
import quickfix.Message;

public interface ExecutionReportBuilder {
	Message ack(Message newOrderSingle, String orderID, String execID)
			throws FieldNotFound;

	Message fill(Message newOrderSingle, String orderID, String execID,
			double cumQty, double avgPx, double lastShares, double lastPx)
			throws FieldNotFound;
}
