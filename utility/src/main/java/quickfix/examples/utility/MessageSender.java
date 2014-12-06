package quickfix.examples.utility;

import quickfix.Message;

public interface MessageSender {
	boolean sendToTarget(Message message);
}
