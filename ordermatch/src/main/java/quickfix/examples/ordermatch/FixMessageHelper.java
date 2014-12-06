package quickfix.examples.ordermatch;

import quickfix.FieldNotFound;
import quickfix.Message;
import quickfix.field.AvgPx;
import quickfix.field.CumQty;
import quickfix.field.ExecID;
import quickfix.field.ExecTransType;
import quickfix.field.ExecType;
import quickfix.field.IDSource;
import quickfix.field.LastPx;
import quickfix.field.LastShares;
import quickfix.field.LeavesQty;
import quickfix.field.OrdStatus;
import quickfix.field.OrderID;
import quickfix.field.SecurityID;
import quickfix.field.SenderCompID;
import quickfix.field.TargetCompID;
import quickfix.field.Text;
import quickfix.fix42.ExecutionReport;
import quickfix.fix42.NewOrderSingle;
import quickfix.fix42.OrderCancelRequest;

public class FixMessageHelper {
	public static ExecutionReport reject(String execID, String orderID,
			NewOrderSingle request, String message) throws FieldNotFound {

		ExecutionReport fixOrder = new ExecutionReport(new OrderID(orderID),
				new ExecID(execID), new ExecTransType(ExecTransType.NEW),
				new ExecType(ExecType.REJECTED), new OrdStatus(
						ExecType.REJECTED), request.getSymbol(),
				request.getSide(), new LeavesQty(0), new CumQty(0),
				new AvgPx(0));

		fixOrder.setString(Text.FIELD, message);

		copy(request, fixOrder);
		return fixOrder;
	}

	public static ExecutionReport updateOrder(String execID, String orderID,
			Order order, char status, NewOrderSingle request)
			throws FieldNotFound {
		ExecutionReport fixOrder = new ExecutionReport(new OrderID(orderID),
				new ExecID(execID), new ExecTransType(ExecTransType.NEW),
				new ExecType(status), new OrdStatus(status),
				request.getSymbol(), request.getSide(), new LeavesQty(
						order.getOpenQuantity()), new CumQty(
						order.getExecutedQuantity()), new AvgPx(
						order.getAvgExecutedPrice()));

		fixOrder.setField(request.getOrderQty());

		if (status == OrdStatus.FILLED || status == OrdStatus.PARTIALLY_FILLED) {
			fixOrder.setDouble(LastShares.FIELD,
					order.getLastExecutedQuantity());
			fixOrder.setDouble(LastPx.FIELD, order.getPrice());
		}

		copy(request, fixOrder);

		return fixOrder;
	}

	public static ExecutionReport updateOrder(String execID, String orderID,
			Order order, char status, OrderCancelRequest request)
			throws FieldNotFound {
		ExecutionReport fixOrder = new ExecutionReport(new OrderID(orderID),
				new ExecID(execID), new ExecTransType(ExecTransType.NEW),
				new ExecType(status), new OrdStatus(status),
				request.getSymbol(), request.getSide(), new LeavesQty(0),
				new CumQty(order.getExecutedQuantity()), new AvgPx(
						order.getAvgExecutedPrice()));

		fixOrder.setField(request.getOrderQty());

		if (status == OrdStatus.FILLED || status == OrdStatus.PARTIALLY_FILLED) {
			fixOrder.setDouble(LastShares.FIELD, 0);
			fixOrder.setDouble(LastPx.FIELD, 0.0);
		}

		copy(request, fixOrder);

		return fixOrder;
	}

	private static void reverseRoute(Message message, Message reply)
			throws FieldNotFound {
		reply.getHeader().setString(SenderCompID.FIELD,
				message.getHeader().getString(TargetCompID.FIELD));
		reply.getHeader().setString(TargetCompID.FIELD,
				message.getHeader().getString(SenderCompID.FIELD));
	}

	private static void copy(NewOrderSingle request, ExecutionReport fixOrder)
			throws FieldNotFound {
		reverseRoute(request, fixOrder);
		fixOrder.setField(request.getClOrdID());
		if (fixOrder.isSetField(SecurityID.FIELD)) {
			fixOrder.setField(request.getSecurityID());
		}
		if (fixOrder.isSetField(IDSource.FIELD)) {
			fixOrder.setField(request.getIDSource());
		}

	}

	private static void copy(OrderCancelRequest request,
			ExecutionReport fixOrder) throws FieldNotFound {
		reverseRoute(request, fixOrder);
		fixOrder.setField(request.getClOrdID());
		fixOrder.setField(request.getOrigClOrdID());
		if (fixOrder.isSetField(SecurityID.FIELD)) {
			fixOrder.setField(request.getSecurityID());
		}
		if (fixOrder.isSetField(IDSource.FIELD)) {
			fixOrder.setField(request.getIDSource());
		}

	}
}
