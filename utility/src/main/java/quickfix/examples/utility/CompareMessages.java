package quickfix.examples.utility;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.junit.Assert;

import quickfix.InvalidMessage;
import quickfix.Message;

public class CompareMessages {
	private static String SEPARATOR = "\001";
	private static int[] excludeTags = { 9, 10, 11, 17, 19, 37, 41, 52, 60 };

	public static void compareMessages(List<Message> actualMessages,
			char delimiter, List<String> expectedMessageStrs)
			throws InvalidMessage {
		compareMessages(actualMessages, delimiter,
				expectedMessageStrs.toArray(new String[0]));
	}

	public static void compareMessages(List<Message> actualMessages,
			char delimiter, String... expectedMessageStrs)
			throws InvalidMessage {
		assertEquals("Number of message mistach", expectedMessageStrs.length,
				actualMessages.size());

		for (int i = 0; i < expectedMessageStrs.length; i++) {
			Message actualMessage = actualMessages.get(i);
			Message expectedMessage = FixMessageUtil.parse(
					expectedMessageStrs[i], delimiter);
			Tuple2<Integer, String> firstDiff = include(actualMessage,
					expectedMessage);
			if (firstDiff != null) {
				Assert.fail("Message different: " + firstDiff + ", "
						+ actualMessage);
			}
		}
	}

	public static Tuple2<Integer, String> include(Message message1,
			Message message2) {
		return include(message1, message2, excludeTags);
	}

	public static Tuple2<Integer, String> include(Message message1,
			Message message2, int[] excludeTags) {
		List<Tuple2<Integer, String>> tuples1 = parse(message1.toString());
		List<Tuple2<Integer, String>> tuples2 = parse(message2.toString());

		return include(tuples1, tuples2, excludeTags);
	}

	private static Tuple2<Integer, String> include(
			List<Tuple2<Integer, String>> tuples1,
			List<Tuple2<Integer, String>> tuples2, int[] excludeTags) {
		Set<Integer> excludes = new HashSet<Integer>();
		for (int tag : excludeTags) {
			excludes.add(tag);
		}

		for (Tuple2<Integer, String> tuple2 : tuples2) {
			if (!excludes.contains(tuple2.getFirst())
					&& !tuples1.contains(tuple2)) {
				return tuple2;
			}
		}
		return null;
	}

	private static List<Tuple2<Integer, String>> parse(String text) {
		String[] ss = text.split(SEPARATOR);

		List<Tuple2<Integer, String>> result = new LinkedList<Tuple2<Integer, String>>();
		for (String s : ss) {
			int index = s.indexOf('=');
			int tag = Integer.parseInt(s.substring(0, index));
			String value = s.substring(index + 1);
			Tuple2<Integer, String> t = new Tuple2<Integer, String>(tag, value);
			result.add(t);
		}
		return result;
	}
}
