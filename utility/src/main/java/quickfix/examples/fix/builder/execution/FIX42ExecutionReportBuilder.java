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
import quickfix.field.Price;
import quickfix.field.Text;
import quickfix.fix42.ExecutionReport;
import quickfix.fix42.NewOrderSingle;
import quickfix.fix42.OrderCancelReplaceRequest;
import quickfix.fix42.OrderCancelRequest;

public class FIX42ExecutionReportBuilder extends AbstractExecutioReportBuilder {

	public Message orderAcked(Message message, String orderID, String execID)
			throws FieldNotFound {
		NewOrderSingle order = (NewOrderSingle) message;
		ExecutionReport accept = new ExecutionReport(new OrderID(orderID),
				new ExecID(execID), new ExecTransType(ExecTransType.NEW),
				new ExecType(ExecType.NEW), new OrdStatus(OrdStatus.NEW),
				order.getSymbol(), order.getSide(), new LeavesQty(order
						.getOrderQty().getValue()), new CumQty(0), new AvgPx(0));
		
		accept.setField(order.getOrderQty());
		accept.set(order.getClOrdID());
		accept.set(new LastShares(0));
		accept.set(new LastPx(0));

		reverseRoute(message, accept);
		return accept;
	}

	public Message orderRejected(Message message, String orderID, String execID,
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

	public Message fillOrder(Message message, String orderID, String execID,
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

	public Message orderCanceled(Message message, String orderID, String execID,
			double cumQty, double avgPx) throws FieldNotFound {
		OrderCancelRequest request = (OrderCancelRequest) message;
		ExecutionReport fixOrder = new ExecutionReport(new OrderID(orderID),
				new ExecID(execID), new ExecTransType(ExecTransType.NEW),
				new ExecType(OrdStatus.CANCELED), new OrdStatus(
						OrdStatus.CANCELED), request.getSymbol(),
				request.getSide(), new LeavesQty(0), new CumQty(cumQty),
				new AvgPx(avgPx));

		fixOrder.setField(request.getClOrdID());
		fixOrder.setField(request.getOrigClOrdID());
		fixOrder.setField(request.getOrderQty());
		reverseRoute(message, fixOrder);
		return fixOrder;
	}

	@Override
	public Message orderReplaced(Message message, String orderID, String execID,
			double cumQty, double avgPx) throws FieldNotFound {
		OrderCancelReplaceRequest request = (OrderCancelReplaceRequest) message;
		ExecutionReport fixOrder = new ExecutionReport(new OrderID(orderID),
				new ExecID(execID), new ExecTransType(ExecTransType.NEW),
				new ExecType(OrdStatus.REPLACED), new OrdStatus(
						OrdStatus.REPLACED), request.getSymbol(),
				request.getSide(), new LeavesQty(request.getOrderQty()
						.getValue() - cumQty), new CumQty(cumQty), new AvgPx(
						avgPx));

		fixOrder.setField(request.getClOrdID());
		fixOrder.setField(request.getOrigClOrdID());
		fixOrder.setField(request.getOrderQty());
		fixOrder.setField(request.getOrdType());
		if (request.isSetField(Price.FIELD)) {
			fixOrder.setField(request.getPrice());
		}
		reverseRoute(message, fixOrder);
		return fixOrder;
	}

}
