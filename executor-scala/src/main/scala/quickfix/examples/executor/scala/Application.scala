package quickfix.examples.executor.scala

import quickfix.IncorrectTagValue
import quickfix.Message
import quickfix.SessionID
import quickfix.SessionSettings
import quickfix.examples.fix.builder.execution.ExecutionReportBuilderFactory
import quickfix.examples.utility.scala.Logging
import quickfix.examples.utility.CrackedApplicationAdapter
import quickfix.examples.utility.DefaultMessageSender
import quickfix.examples.utility.IdGenerator
import quickfix.examples.utility.MessageSender
import quickfix.field.BeginString
import quickfix.field.OrdStatus
import quickfix.field.OrdType
import quickfix.field.OrderQty
import quickfix.field.Price
import quickfix.field.Side
import quickfix.field.Symbol

class Application(alwaysFillLimitOrders: Boolean,
                  validOrderTypesStr: String, defaultMarketPrice: Double, messageSender: MessageSender) extends CrackedApplicationAdapter with Logging {
  private val DEFAULT_MARKET_PRICE_KEY = "DefaultMarketPrice";
  private val ALWAYS_FILL_LIMIT_KEY = "AlwaysFillLimitOrders";
  private val VALID_ORDER_TYPES_KEY = "ValidOrderTypes";

  private val validOrderTypes: Set[String] = {
    if (validOrderTypesStr != null && validOrderTypesStr.trim().length() > 0) {
      validOrderTypesStr.trim().split("\\s*,\\s*").toSet
    } else {
      Set(OrdType.LIMIT + "");
    }
  }

  var marketDataProvider: MarketDataProvider = {
    new MarketDataProvider() {
      def getAsk(symbol: String): Double = {
        defaultMarketPrice;
      }

      def getBid(symbol: String): Double = {
        defaultMarketPrice;
      }
    }
  }

  private val builderFactory = new ExecutionReportBuilderFactory();
  private val idGenerator = new IdGenerator();

  def this(alwaysFillLimitOrders: Boolean,
           validOrderTypesStr: String, defaultMarketPrice: Double) {
    this(alwaysFillLimitOrders, validOrderTypesStr, defaultMarketPrice, new DefaultMessageSender())
  }

  def onMessage(order: quickfix.fix40.NewOrderSingle,
                sessionID: SessionID): Unit = {
    onNewOrder(order, sessionID)
  }

  def onMessage(order: quickfix.fix41.NewOrderSingle,
                sessionID: SessionID): Unit = {
    onNewOrder(order, sessionID)
  }

  def onMessage(order: quickfix.fix42.NewOrderSingle,
                sessionID: SessionID): Unit = {
    onNewOrder(order, sessionID)
  }

  def onMessage(order: quickfix.fix43.NewOrderSingle,
                sessionID: SessionID): Unit = {
    onNewOrder(order, sessionID)
  }

  def onMessage(order: quickfix.fix44.NewOrderSingle,
                sessionID: SessionID): Unit = {
    onNewOrder(order, sessionID)
  }

  def onMessage(order: quickfix.fix50.NewOrderSingle,
                sessionID: SessionID): Unit = {
    onNewOrder(order, sessionID)
  }

  private def onNewOrder(order: Message, sessionID: SessionID): Unit = {
    validateOrder(order);

    val send: (Message => Unit) = messageSender.sendMessage(_, sessionID);

    val beginStr = order.getHeader().getString(BeginString.FIELD);
    val builder = builderFactory.getExecutionReportBuilder(beginStr)

    val orderID = idGenerator.genOrderID()
    val ackMsg = builder.orderAcked(order, orderID, idGenerator.genExecID())
    send(ackMsg);

    val price = getPrice(order)
    if (isOrderExecutable(order, price)) {
      val orderQty = new OrderQty();
      order.getField(orderQty);
      val orderQtyValue = orderQty.getValue
      val priceValue = price.getValue
      val fillMsg = builder.fillOrder(order, orderID, idGenerator.genExecID(), OrdStatus.FILLED,
        orderQtyValue, priceValue, orderQtyValue, priceValue);
      send(fillMsg);
    }
  }

  def isOrderExecutable(order: Message, price: Price): Boolean = {
    if (order.getChar(OrdType.FIELD) == OrdType.LIMIT) {
      val limitPrice = order.getDouble(Price.FIELD);
      val side = order.getChar(Side.FIELD)
      val dPrice = price.getValue

      return (side == Side.BUY && dPrice <= limitPrice) ||
        ((side == Side.SELL || side == Side.SELL_SHORT) && dPrice >= limitPrice);
    }
    true
  }

  def getPrice(message: Message): Price = {
    if (message.getChar(OrdType.FIELD) == OrdType.LIMIT && alwaysFillLimitOrders) {
      val price = new Price()
      message.getField(price)
      price
    } else {
      val side = message.getChar(Side.FIELD);
      val symbol = message.getString(Symbol.FIELD)
      quotePrice(side, symbol);
    }
  }

  private def quotePrice(side: Char, symbol: String): Price = {
    if (side == Side.BUY) {
      new Price(marketDataProvider.getAsk(symbol));
    } else if (side == Side.SELL || side == Side.SELL_SHORT) {
      new Price(marketDataProvider.getBid(symbol));
    } else {
      throw new RuntimeException("Invalid order side: " + side);
    }
  }

  def validateOrder(order: Message): Unit = {
    val ordType = order.getField(new OrdType())
    if (!validOrderTypes.contains(Character.toString(ordType.getValue()))) {
      log.error("Order type not in ValidOrderTypes setting")
      throw new IncorrectTagValue(ordType.getField())
    }
    if (ordType.getValue() == OrdType.MARKET && marketDataProvider == null) {
      log.error("DefaultMarketPrice setting not specified for market order")
      throw new IncorrectTagValue(ordType.getField())
    }
  }
}