package com.loyal3.sms.core.util.healthcheck

import java.util.concurrent.Executors

import com.loyal3.service.health.{Warning, HealthCheckResult}
import com.twitter.util.FuturePool

import scala.util.{Failure, Success, Try}

trait HealthCheckUtils {
  private val threadPoolSize: Int = 10

  val pool: FuturePool = FuturePool(Executors.newFixedThreadPool(threadPoolSize))

  def tryHealthCheck[T >: HealthCheckResult](check: => T)(err: Throwable => String): T = {
    Try(check) match {
      case Success(s) => s
      case Failure(e) => Warning(err(e))
    }
  }
}

object HealthCheckUtils extends HealthCheckUtils
