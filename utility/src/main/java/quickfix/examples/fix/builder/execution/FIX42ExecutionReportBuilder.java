package quickfix.examples.fix.builder.execution;

import quickfix.FieldNotFound;
import quickfix.Message;
import quickfix.field.AvgPx;
import quickfix.field.CumQty;
import quickfix.field.ExecID;
import quickfix.field.ExecTransType;
import quickfix.field.ExecType;
import quickfix.field.LastPx;
import quickfix.field.LastShares;
import quickfix.field.LeavesQty;
import quickfix.field.OrdStatus;
import quickfix.field.OrderID;
import quickfix.field.OrderQty;
import quickfix.field.Text;
import quickfix.fix42.ExecutionReport;
import quickfix.fix42.NewOrderSingle;
import quickfix.fix42.OrderCancelRequest;

public class FIX42ExecutionReportBuilder extends AbstractExecutioReportBuilder {

	public Message ack(Message message, String orderID, String execID)
			throws FieldNotFound {
		NewOrderSingle order = (NewOrderSingle) message;
		ExecutionReport accept = new ExecutionReport(new OrderID(orderID),
				new ExecID(execID), new ExecTransType(ExecTransType.NEW),
				new ExecType(ExecType.NEW), new OrdStatus(OrdStatus.NEW),
				order.getSymbol(), order.getSide(), new LeavesQty(order
						.getOrderQty().getValue()), new CumQty(0), new AvgPx(0));
		accept.setField(order.getOrderQty());
		accept.set(order.getClOrdID());

		reverseRoute(message, accept);
		return accept;
	}

	public Message reject(Message message, String orderID, String execID,
			String text) throws FieldNotFound {
		NewOrderSingle request = (NewOrderSingle) message;
		ExecutionReport fixOrder = new ExecutionReport(new OrderID(orderID),
				new ExecID(execID), new ExecTransType(ExecTransType.NEW),
				new ExecType(ExecType.REJECTED), new OrdStatus(
						ExecType.REJECTED), request.getSymbol(),
				request.getSide(), new LeavesQty(0), new CumQty(0),
				new AvgPx(0));
		fixOrder.setField(request.getOrderQty());
		fixOrder.setString(Text.FIELD, text);
		reverseRoute(message, fixOrder);
		return fixOrder;
	}

	public Message fill(Message message, String orderID, String execID,
			char ordStatus, double cumQty, double avgPx, double lastShares,
			double lastPx) throws FieldNotFound {
		NewOrderSingle order = (NewOrderSingle) message;
		OrderQty orderQty = order.getOrderQty();
		char execType = cumQty < orderQty.getValue() ? ExecType.PARTIAL_FILL
				: ExecType.FILL;

		ExecutionReport executionReport = new ExecutionReport(new OrderID(
				orderID), new ExecID(execID), new ExecTransType(
				ExecTransType.NEW), new ExecType(execType), new OrdStatus(
				ordStatus), order.getSymbol(), order.getSide(), new LeavesQty(
				orderQty.getValue() - cumQty), new CumQty(orderQty.getValue()),
				new AvgPx(avgPx));

		executionReport.set(order.getClOrdID());
		executionReport.set(orderQty);
		executionReport.set(new LastShares(lastShares));
		executionReport.set(new LastPx(lastPx));
		reverseRoute(message, executionReport);
		return executionReport;
	}

	public Message canceled(Message message, String orderID, String execID,
			double cumQty, double avgPx) throws FieldNotFound {
		OrderCancelRequest request = (OrderCancelRequest) message;
		ExecutionReport fixOrder = new ExecutionReport(new OrderID(orderID),
				new ExecID(execID), new ExecTransType(ExecTransType.NEW),
				new ExecType(OrdStatus.CANCELED), new OrdStatus(
						OrdStatus.CANCELED), request.getSymbol(),
				request.getSide(), new LeavesQty(0), new CumQty(cumQty),
				new AvgPx(avgPx));

		fixOrder.setField(request.getOrderQty());
		reverseRoute(message, fixOrder);
		return fixOrder;
	}

}
