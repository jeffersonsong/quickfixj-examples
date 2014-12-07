package quickfix.examples.fix.builder.execution;

import quickfix.FieldNotFound;
import quickfix.Message;

public interface ExecutionReportBuilder {
	Message orderAcked(Message newOrderSingle, String orderID, String execID)
			throws FieldNotFound;
	
	Message orderRejected(Message newOrderSingle, String orderID, String execID,
			String text) throws FieldNotFound ;

	Message fillOrder(Message newOrderSingle, String orderID, String execID,
			char ordStatus, double cumQty, double avgPx, double lastShares,
			double lastPx) throws FieldNotFound;
	
	Message orderCanceled(Message cancelRequest, String orderID, String execID, 
			double cumQty, double avgPx) throws FieldNotFound;
	
	Message orderReplaced(Message replaceRequest, String orderID, String execID, 
			double cumQty, double avgPx) throws FieldNotFound;
}
