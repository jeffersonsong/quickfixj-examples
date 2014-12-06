package quickfix.examples.fix.builder.execution;

import quickfix.FieldNotFound;
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
import quickfix.fix42.ExecutionReport;
import quickfix.fix42.NewOrderSingle;

public class FIX42ExecutionReportBuilder implements
		ExecutionReportBuilder<ExecutionReport, NewOrderSingle> {

	public ExecutionReport ack(NewOrderSingle order, String orderID,
			String execID) throws FieldNotFound {
		ExecutionReport accept = new ExecutionReport(new OrderID(orderID),
				new ExecID(execID), new ExecTransType(ExecTransType.NEW),
				new ExecType(ExecType.NEW), new OrdStatus(OrdStatus.NEW),
				order.getSymbol(), order.getSide(), new LeavesQty(0),
				new CumQty(0), new AvgPx(0));
		accept.set(order.getClOrdID());
		return accept;
	}

	public ExecutionReport fill(NewOrderSingle order, String orderID,
			String execID, double cumQty, double avgPx, double lastShares,
			double lastPx) throws FieldNotFound {
		OrderQty orderQty = order.getOrderQty();
		char ordStatus = cumQty < orderQty.getValue() ? OrdStatus.PARTIALLY_FILLED
				: OrdStatus.FILLED;
		char execType = ordStatus == OrdStatus.PARTIALLY_FILLED ? ExecType.PARTIAL_FILL
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
		return executionReport;
	}

}
