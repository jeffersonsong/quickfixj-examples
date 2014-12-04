package quickfix.examples.banzai.model;

import java.util.HashMap;
import java.util.Map;

public class OrderTIFParser {
	private static Map<String, OrderTIF> known = new HashMap<String, OrderTIF>();

	static {
		for (OrderTIF type : OrderTIF.values()) {
			known.put(type.getName(), type);
		}
	}

	public static OrderTIF parse(String type) throws IllegalArgumentException {
		OrderTIF result = known.get(type);
		if (result == null) {
			throw new IllegalArgumentException("OrderTIF:  " + type
					+ " is unknown.");
		}
		return result;
	}
}
