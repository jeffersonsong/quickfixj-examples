package quickfix.examples.executor.scala

trait MarketDataProvider {
  def getBid(symbol: String): Double
  def getAsk(symbol: String): Double
}