package quickfix.examples.banzai.model;

import java.util.HashMap;
import java.util.Map;

public class OrderSideParser {
	private static Map<String, OrderSide> known = new HashMap<String, OrderSide>();

	static {
		for (OrderSide side : OrderSide.values()) {
			known.put(side.getName(), side);
		}
	}

	public static OrderSide parseOrderSide(String type) {
		OrderSide result = known.get(type);
		if (result == null) {
			throw new IllegalArgumentException("OrderSide:  " + type
					+ " is unknown.");
		}
		return result;
	}
}
