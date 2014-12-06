package quickfix.examples.fix.builder.execution;

import quickfix.FieldNotFound;
import quickfix.Message;

public interface ExecutionReportBuilder<Exec extends Message, NewOrderSingle extends Message> {
	Exec ack(NewOrderSingle newOrderSingle, String orderID, String execID) throws FieldNotFound;

	Exec fill(NewOrderSingle newOrderSingle, String orderID, String execID,
			double cumQty, double avgPx, double lastShares, double lastPx) throws FieldNotFound;
}
