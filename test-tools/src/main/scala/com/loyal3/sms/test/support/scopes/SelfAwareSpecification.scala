package com.loyal3.sms.test.support.scopes

import org.specs2.mutable.Specification
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import org.specs2.matcher.MustMatchers
import scala.util.Random

trait SelfAwareSpecification
  extends Specification
  with SelfAware
  with AsyncHelpers
  with TimeHelpers
  with StringHelpers

trait AsyncHelpers {
  def waitForCompletionOf[T](future: Future[T]) = Await.result(future, Duration(1, MINUTES))
}

trait TimeHelpers extends MustMatchers {
  def time[T](iterations: Int)(f: => T): (T, Long) = {
    val started = System.currentTimeMillis()
    val result: T = f
    val elapsed = System.currentTimeMillis() - started

    println("%d iterations completed in %dms (%.2fms each)" format(iterations, elapsed, elapsed.toDouble / iterations.toDouble))

    (result, elapsed)
  }

  /**
   * time Less Than
   */
  def timeLessThan[T](millis: Long, iterations: Int = 1)(f: => T): T = {
    val (result, elapsed) = time(iterations) {
      f
    }
    elapsed must (be < millis)
    result
  }

  /**
   * time Greater Than or Equal to
   */
  def timeGreaterThanOrEqual[T](millis: Long, iterations: Int = 1)(f: => T): T = {
    val (result, elapsed) = time(iterations) {
      f
    }
    elapsed must (be >= millis)
    result
  }
}

trait StringHelpers {
  def randomEmail: String = "test-%d-%d@test.loyal3.com".format(System.currentTimeMillis(), Random.nextLong().abs)
}

