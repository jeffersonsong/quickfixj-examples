package quickfix.examples.ordermatch.step_definitions;

import static quickfix.examples.utility.CompareMessages.compareMessages;
import static quickfix.examples.utility.FixMessageUtil.parse;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quickfix.InvalidMessage;
import quickfix.Message;
import quickfix.examples.ordermatch.Application;
import quickfix.examples.utility.MockMessageSender;
import quickfix.field.MsgType;
import quickfix.fix42.NewOrderSingle;
import quickfix.fix42.OrderCancelRequest;
import cucumber.api.DataTable;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;

public class MarketSteps {
	private static final Logger log = LoggerFactory
			.getLogger(MarketSteps.class);
	private MockMessageSender messageSender;

	private Application application;

	public MarketSteps() {
		this.messageSender = new MockMessageSender();
		this.application = new Application(messageSender);
	}

	@Given("^the following messages are sent to the match engine:$")
	public void the_following_messages_are_sent_to_the_match_engine(
			DataTable messageTable) throws Exception {
		List<Message> messages = convertToMessages(messageTable);
		for (Message message : messages) {
			String msgType = message.getHeader().getString(MsgType.FIELD);
			if ("D".equals(msgType)) {
				application.onMessage((NewOrderSingle) message, null);
			} else if ("F".equals(msgType)) {
				application.onMessage((OrderCancelRequest) message, null);
			}
			log.info(message.toString());
		}
	}

	@Then("^the match engine returns messages:$")
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
