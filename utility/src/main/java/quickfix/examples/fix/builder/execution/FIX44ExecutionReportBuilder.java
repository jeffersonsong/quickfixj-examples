package quickfix.examples.fix.builder.execution;

import quickfix.FieldNotFound;
import quickfix.Message;
import quickfix.field.AvgPx;
import quickfix.field.CumQty;
import quickfix.field.ExecID;
import quickfix.field.ExecType;
import quickfix.field.LastPx;
import quickfix.field.LastQty;
import quickfix.field.LeavesQty;
import quickfix.field.OrdStatus;
import quickfix.field.OrderID;
import quickfix.field.OrderQty;
import quickfix.fix44.ExecutionReport;
import quickfix.fix44.NewOrderSingle;

public class FIX44ExecutionReportBuilder extends AbstractExecutioReportBuilder {

	public Message orderAcked(Message message, String orderID, String execID)
			throws FieldNotFound {
		NewOrderSingle order = (NewOrderSingle) message;
		ExecutionReport accept = new ExecutionReport(new OrderID(orderID),
				new ExecID(execID), new ExecType(ExecType.NEW), new OrdStatus(
						OrdStatus.NEW), order.getSide(), new LeavesQty(order
						.getOrderQty().getValue()), new CumQty(0), new AvgPx(0));

		accept.set(order.getClOrdID());
		accept.set(order.getSymbol());
		accept.setField(order.getOrderQty());
		accept.set(new LastQty(0));
		accept.set(new LastPx(0));
		
		reverseRoute(message, accept);
		return accept;
	}

	public Message fillOrder(Message message, String orderID, String execID,
			char ordStatus, double cumQty, double avgPx, double lastShares,
			double lastPx) throws FieldNotFound {
		NewOrderSingle order = (NewOrderSingle) message;
		OrderQty orderQty = order.getOrderQty();
		char execType = cumQty < orderQty.getValue() ? ExecType.PARTIAL_FILL
				: ExecType.FILL;

		ExecutionReport executionReport = new ExecutionReport(new OrderID(
				orderID), new ExecID(execID), new ExecType(execType),
				new OrdStatus(ordStatus), order.getSide(), new LeavesQty(
						orderQty.getValue() - cumQty), new CumQty(cumQty),
				new AvgPx(avgPx));

		executionReport.set(order.getClOrdID());
		executionReport.set(order.getSymbol());
		executionReport.set(orderQty);
		executionReport.set(new LastQty(lastShares));
		executionReport.set(new LastPx(lastPx));
		
		reverseRoute(message, executionReport);
		return executionReport;
	}

}
