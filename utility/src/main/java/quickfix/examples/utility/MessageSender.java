package quickfix.examples.utility;

import quickfix.Message;
import quickfix.SessionNotFound;

public interface MessageSender {
	boolean sendToTarget(Message message) throws SessionNotFound;
}
