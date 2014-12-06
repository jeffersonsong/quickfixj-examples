package quickfix.examples.fix.builder.execution;

import quickfix.FieldNotFound;
import quickfix.field.AvgPx;
import quickfix.field.CumQty;
import quickfix.field.ExecID;
import quickfix.field.ExecTransType;
import quickfix.field.LastPx;
import quickfix.field.LastShares;
import quickfix.field.OrdStatus;
import quickfix.field.OrderID;
import quickfix.field.OrderQty;
import quickfix.fix40.ExecutionReport;
import quickfix.fix40.NewOrderSingle;

public class FIX40ExecutionReportBuilder implements
		ExecutionReportBuilder<ExecutionReport, NewOrderSingle> {

	public ExecutionReport ack(NewOrderSingle order, String orderID,
			String execID) throws FieldNotFound {
		OrderQty orderQty = order.getOrderQty();
		ExecutionReport accept = new ExecutionReport(new OrderID(orderID),
				new ExecID(execID), new ExecTransType(ExecTransType.NEW),
				new OrdStatus(OrdStatus.NEW), order.getSymbol(),
				order.getSide(), orderQty, new LastShares(0), new LastPx(0),
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
		ExecutionReport fill = new ExecutionReport(new OrderID(orderID),
				new ExecID(execID), new ExecTransType(ExecTransType.NEW),
				new OrdStatus(ordStatus), order.getSymbol(), order.getSide(),
				orderQty, new LastShares(lastShares), new LastPx(lastPx),
				new CumQty(cumQty), new AvgPx(avgPx));

		fill.set(order.getClOrdID());
		return fill;
	}
}
