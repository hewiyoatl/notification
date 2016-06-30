package com.loyal3.sms.core.util

import com.twitter.util.{Future, FuturePool}
import java.util.concurrent.Executors

object Futures {
  private[this] val pool = FuturePool(Executors.newFixedThreadPool(Runtime.getRuntime.availableProcessors * 2))

  def apply[T](f: => T): Future[T] = {
    pool {
      f
    }
  }
}
