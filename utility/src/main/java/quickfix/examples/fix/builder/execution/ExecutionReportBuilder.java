package quickfix.examples.fix.builder.execution;

import quickfix.FieldNotFound;
import quickfix.Message;

public interface ExecutionReportBuilder {
	Message ack(Message newOrderSingle, String orderID, String execID)
			throws FieldNotFound;
	
	Message reject(Message newOrderSingle, String orderID, String execID,
			String text) throws FieldNotFound ;

	Message fill(Message newOrderSingle, String orderID, String execID,
			char ordStatus, double cumQty, double avgPx, double lastShares,
			double lastPx) throws FieldNotFound;
	
	Message canceled(Message cancelRequest, String orderID, String execID, 
			double cumQty, double avgPx) throws FieldNotFound;
	
	Message replaced(Message replaceRequest, String orderID, String execID, 
			double cumQty, double avgPx) throws FieldNotFound;
}
