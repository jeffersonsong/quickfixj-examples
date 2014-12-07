package quickfix.examples.executor.step_definitions;

import static quickfix.examples.utility.CompareMessages.compareMessages;
import static quickfix.examples.utility.FixMessageUtil.parse;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quickfix.InvalidMessage;
import quickfix.Message;
import quickfix.examples.executor.Application;
import quickfix.examples.executor.MockMessageSender;
import cucumber.api.DataTable;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;

public class ExecutorSteps {
	private static final Logger log = LoggerFactory
			.getLogger(ExecutorSteps.class);
	private MockMessageSender messageSender;
	private Application application;

	public ExecutorSteps() throws Exception {
		super();
		this.messageSender = new MockMessageSender();
		this.application = new Application(false, "1,2,F", 12.30, messageSender);
	}

	@Given("^the following messages are sent to the executor:$")
	public void the_following_messages_are_sent_to_the_match_engine(
			DataTable messageTable) throws Exception {
		List<Message> messages = convertToMessages(messageTable);
		for (Message message : messages) {
			application.fromApp(message, null);
			log.info(message.toString());
		}
	}

	@Then("^the executor returns following messages:$")
	public void the_match_engine_returns_messages(DataTable messageTable)
			throws Exception {
		String[] expectedMessageStrs = messageTable.asList(String.class)
				.toArray(new String[0]);
		List<Message> actualMessages = messageSender.fetchAndEmpty();
		compareMessages(actualMessages, '^', expectedMessageStrs);
	}

	private List<Message> convertToMessages(DataTable messageTable)
			throws InvalidMessage {
		List<String> textStrs = messageTable.asList(String.class);
		List<Message> messages = new ArrayList<Message>(textStrs.size());
		for (String textStr : textStrs) {
			Message message = parse(textStr, '^');
			messages.add(message);
		}
		return messages;
	}
}
