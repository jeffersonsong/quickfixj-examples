package quickfix.examples.executor.scala.step_definitions

import scala.collection.JavaConversions._
import cucumber.api.DataTable
import quickfix.Message
import quickfix.examples.executor.scala.Application
import quickfix.examples.utility.FixMessageUtil
import quickfix.examples.utility.MockMessageSender
import quickfix.examples.utility.scala.Logging
import cucumber.api.java.en.Given
import cucumber.api.java.en.Then;
import quickfix.examples.utility.CompareMessages

class ExecutorSteps extends Logging {
  private val messageSender = new MockMessageSender()
  private val application = new Application(false, "1,2,F", 12.30, messageSender)

  @Given("^the following messages are sent to the executor:$")
  def the_following_messages_are_sent_to_the_match_engine(
    messageTable: DataTable): Unit = {

    val messages = convertToMessages(messageTable);
    messages.foreach { message =>
      {
        application.fromApp(message, null); log.info(message.toString)
      }
    }
  }

  @Then("^the executor returns following messages:$")
  def the_match_engine_returns_messages(messageTable: DataTable): Unit = {
    val expectedMessageStrs = messageTable.asList(classOf[String])
    val actualMessages = messageSender.fetchAndEmpty()
    CompareMessages.compareMessages(actualMessages, '^', expectedMessageStrs)
  }

  private def convertToMessages(messageTable: DataTable): List[Message] = {
    messageTable.asList[String](classOf[String]).toList.map { FixMessageUtil.parse(_, '^') }
  }
}