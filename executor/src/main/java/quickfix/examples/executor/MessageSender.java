package quickfix.examples.executor;

import quickfix.Message;
import quickfix.SessionID;

public interface MessageSender {
	void sendMessage(SessionID sessionID, Message message);
}
