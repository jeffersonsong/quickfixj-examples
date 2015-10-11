package quickfix.examples.utility.scala

import org.slf4j.LoggerFactory

trait Logging {
  val log = LoggerFactory.getLogger(this.getClass)
}