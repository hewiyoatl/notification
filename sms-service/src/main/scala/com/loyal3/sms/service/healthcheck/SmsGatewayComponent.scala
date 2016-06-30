package com.loyal3.sms.service.healthcheck

import java.util.concurrent.{ExecutorService, Executors}
import com.loyal3.service.health._
import com.loyal3.sms.config.Config
import com.loyal3.sms.core.util.healthcheck.HealthCheckUtils
import com.loyal3.sms.service.provider.twilio.TwilioProvider
import com.twitter.util.Future
import scala.concurrent.ExecutionContext

class SmsGatewayComponent {
  val executor: ExecutorService = Executors.newFixedThreadPool(Config.current.provider.maxConnectionPerRoute)
  val executionContext: ExecutionContext = ExecutionContext.fromExecutorService(executor)
  val provider: TwilioProvider = new TwilioProvider(accountSid  = Config.current.provider.accountSid,
                                                    authToken   = Config.current.provider.authToken,
                                                    execContext = executionContext)

  private[service] val healthUtils: HealthCheckUtils = HealthCheckUtils

  val connectionHealthCheck: HealthCheckable = new HealthCheckable {
    override val name: String = "SMS Provider Pool Check"
    override val description: String = "Ensures SMS provider pool is in good shape."
    override def checkHealth(): Future[HealthCheckResult] = checkPoolHealth()
  }

  private def checkPoolHealth(): Future[HealthCheckResult] = healthUtils.pool {
    healthUtils.tryHealthCheck(provider.healthCheckPool)(_.getMessage)
  }

}
