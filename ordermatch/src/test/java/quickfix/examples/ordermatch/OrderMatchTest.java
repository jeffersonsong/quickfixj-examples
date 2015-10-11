package quickfix.examples.ordermatch;

import static quickfix.examples.utility.CompareMessages.compareMessages;
import static quickfix.examples.utility.FixMessageUtil.parse;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import quickfix.Message;
import quickfix.SessionID;
import quickfix.examples.utility.MockMessageSender;
import quickfix.fix42.NewOrderSingle;

public class OrderMatchTest {
	@Mock
	private SessionID sessionID;
	private MockMessageSender messageSender;

	private Application application;

	@Before
	public void setUp() throws Exception {
		messageSender = new MockMessageSender();
		application = new Application(messageSender);
	}

	@Test
	public void test() throws Exception {
		Message buyMsg = parse(
				"8=FIX.4.2^35=D^11=0001^49=A^56=B^38=100^40=2^44=10.5^54=1^55=CSCO",
				'^');
		Message sellMsg = parse(
				"8=FIX.4.2^35=D^11=0001^49=A^56=B^38=100^40=2^44=10.5^54=2^55=CSCO",
				'^');

		application.onMessage((NewOrderSingle) buyMsg, sessionID);
		List<Message> messages = messageSender.fetchAndEmpty();

		compareMessages(
				messages,
				'^',
				"8=FIX.4.2^35=8^49=B^56=A^6=0^11=0001^14=0^17=0^20=0^38=100^39=0^54=1^55=CSCO^150=0^151=100");

		application.onMessage((NewOrderSingle) sellMsg, sessionID);
		messages = messageSender.fetchAndEmpty();

		compareMessages(
				messages,
				'^',
				"8=FIX.4.2^35=8^49=B^56=A^6=0^11=0001^14=0^17=1^20=0^38=100^39=0^54=2^55=CSCO^150=0^151=100",
				"8=FIX.4.2^35=8^49=B^56=A^6=10.5^11=0001^14=100^17=2^20=0^31=10.5^32=100^38=100^39=2^54=2^55=CSCO^150=2^151=0",
				"8=FIX.4.2^35=8^49=B^56=A^6=10.5^11=0001^14=100^17=3^20=0^31=10.5^32=100^38=100^39=2^54=1^55=CSCO^150=2^151=0");
	}
}
