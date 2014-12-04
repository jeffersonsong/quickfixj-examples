package quickfix.examples.banzai.model;

import quickfix.field.OrdType;
import quickfix.field.Side;
import quickfix.field.TimeInForce;

public class TypeMapping {
	static private TwoWayMap sideMap = new TwoWayMap();
	static private TwoWayMap typeMap = new TwoWayMap();
	static private TwoWayMap tifMap = new TwoWayMap();

	public static Side sideToFIXSide(OrderSide side) {
		return (Side) sideMap.getFirst(side);
	}

	public static OrderSide FIXSideToSide(Side side) {
		return (OrderSide) sideMap.getSecond(side);
	}

	public static OrdType typeToFIXType(OrderType type) {
		return (OrdType) typeMap.getFirst(type);
	}

	public static OrderType FIXTypeToType(OrdType type) {
		return (OrderType) typeMap.getSecond(type);
	}

	public static TimeInForce tifToFIXTif(OrderTIF tif) {
		return (TimeInForce) tifMap.getFirst(tif);
	}

	public static OrderTIF FIXTifToTif(TimeInForce tif) {
		return (OrderTIF) typeMap.getSecond(tif);
	}

	static {
		sideMap.put(OrderSide.BUY, new Side(Side.BUY));
		sideMap.put(OrderSide.SELL, new Side(Side.SELL));
		sideMap.put(OrderSide.SHORT_SELL, new Side(Side.SELL_SHORT));
		sideMap.put(OrderSide.SHORT_SELL_EXEMPT, new Side(
				Side.SELL_SHORT_EXEMPT));
		sideMap.put(OrderSide.CROSS, new Side(Side.CROSS));
		sideMap.put(OrderSide.CROSS_SHORT, new Side(Side.CROSS_SHORT));

		typeMap.put(OrderType.MARKET, new OrdType(OrdType.MARKET));
		typeMap.put(OrderType.LIMIT, new OrdType(OrdType.LIMIT));
		typeMap.put(OrderType.STOP, new OrdType(OrdType.STOP));
		typeMap.put(OrderType.STOP_LIMIT, new OrdType(OrdType.STOP_LIMIT));

		tifMap.put(OrderTIF.DAY, new TimeInForce(TimeInForce.DAY));
		tifMap.put(OrderTIF.IOC, new TimeInForce(
				TimeInForce.IMMEDIATE_OR_CANCEL));
		tifMap.put(OrderTIF.OPG, new TimeInForce(TimeInForce.AT_THE_OPENING));
		tifMap.put(OrderTIF.GTC, new TimeInForce(TimeInForce.GOOD_TILL_CANCEL));
		tifMap.put(OrderTIF.GTX,
				new TimeInForce(TimeInForce.GOOD_TILL_CROSSING));
	}
}
