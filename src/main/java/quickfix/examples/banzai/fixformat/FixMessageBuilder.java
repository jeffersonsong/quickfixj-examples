package quickfix.examples.banzai.fixformat;

import quickfix.examples.banzai.Order;

public interface FixMessageBuilder {
	quickfix.Message send(Order order);

	quickfix.Message replace(Order order, Order newOrder);

	quickfix.Message cancel(Order order);
}
