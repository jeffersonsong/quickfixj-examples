package quickfix.examples.banzai.model;

import java.util.HashMap;
import java.util.Map;

public class OrderTypeParser {
	private static Map<String, OrderType> known = new HashMap<String, OrderType>();

	static {
		for (OrderType type : OrderType.values()) {
			known.put(type.getName(), type);
		}
	}

	public static OrderType parseOrderType(String type)
			throws IllegalArgumentException {
		OrderType result = known.get(type);
		if (result == null) {
			throw new IllegalArgumentException("OrderType:  " + type
					+ " is unknown.");
		}
		return result;
	}
}
