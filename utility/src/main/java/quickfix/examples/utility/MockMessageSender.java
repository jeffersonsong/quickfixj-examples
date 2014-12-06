package quickfix.examples.utility;

import java.util.ArrayList;
import java.util.List;

import quickfix.Message;
import quickfix.SessionNotFound;

public class MockMessageSender implements MessageSender {
	private List<Message> messages = new ArrayList<Message>();

	public List<Message> fetchAndEmpty() {
		List<Message> result = messages;
		messages = new ArrayList<Message>();
		return result;
	}

	public boolean sendToTarget(Message message) throws SessionNotFound {
		messages.add(message);
		return true;
	}

}
