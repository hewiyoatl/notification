package com.loyal3.sms.service.server

import java.util.concurrent.Executors
import com.loyal3.service.health._
import com.loyal3.service.health.provider.finatra.HealthCheckController
import com.loyal3.service.status.DefaultConfiguration
import com.loyal3.service.status.provider.finatra.StatusController
import com.loyal3.service.status.services.AppVersionResolver
import com.loyal3.sms.config.Config
import com.loyal3.sms.service.SmsServiceImpl
import com.loyal3.sms.service.controller.SubscriptionController
import com.loyal3.sms.service.controller.twilio.TwilioController
import com.loyal3.sms.service.healthcheck.SmsGatewayComponent
import com.loyal3.sms.service.provider.twilio.TwilioProvider
import com.loyal3.sms.service.repository.RepoManager
import com.twitter.finagle.Filter
import com.twitter.finagle.http.{Request => FinagleRequest, Response => FinagleResponse}
import com.twitter.finatra.FinatraServer
import org.apache.log4j.Logger
import org.jboss.netty.handler.codec.http.{HttpRequest, HttpResponse}
import scala.concurrent.ExecutionContext

object Main {
  private val applicationName: String = "SMS Notification"
  private val applicationVersion: String = AppVersionResolver().getOrElse(DefaultConfiguration.version)

  /**
   * converts netty HttpRequest, HttpResponse objects to their Finagle counterparts
   */
  private[this] val nettyToFinagle =
    Filter.mk[HttpRequest, HttpResponse, FinagleRequest, FinagleResponse] {
      (req, service) =>
        service(FinagleRequest(req)) map {
          _.httpResponse
        }
    }

  def main(args: Array[String]) {

    //Hewi/ Kevin: setting same number of threads to same number of connections to Twilio ensuring no connection timeout
    val logger:Logger = Logger.getLogger(getClass)
    logger.info(s"$applicationName starting..")
    val executor = Executors.newFixedThreadPool(Config.current.provider.maxConnectionPerRoute)
    logger.info("Threadpool created")
    val executionContext: ExecutionContext = ExecutionContext.fromExecutorService(executor)
    try {

      val provider = Config.current.provider.providerType match {
        case twilio => new TwilioProvider(accountSid = Config.current.provider.accountSid,
          authToken = Config.current.provider.authToken,
          execContext = executionContext)
      }
      logger.info("Provider created")
      val smsService = new SmsServiceImpl(provider = provider)
      logger.info("Service created")

      val smsGateway: SmsGatewayComponent = new SmsGatewayComponent
      System.setProperty("local_docroot", System.getProperty("local_docroot", "sms-service/src/main/resources"))

      val server = new FinatraServer()

      logger.info("SERVER: FinatraServer created.")
      logger.info("DB Database: " + Config.current.database.database)
      logger.info("DB host: " + Config.current.database.host)
      logger.info("DB port: " + Config.current.database.port)
      logger.info("DB Username: " + Config.current.database.username)
      logger.info("SMS Provider AccountSID: " + Config.current.provider.accountSid)
      logger.info("SMS Provider areaCode: " + Config.current.provider.areaCode)
      logger.info("SMS Provider fromPhoneNumber: " + Config.current.provider.fromPhoneNumber)
      logger.info("SMS Provider connectionTimeoutInMillis: " + Config.current.provider.connectionTimeoutInMillis)
      logger.info("SMS Provider maxConnectionPerRoute: " + Config.current.provider.maxConnectionPerRoute)
      logger.info("SMS Provider maxUsersPerNumber: " + Config.current.provider.maxUsersPerNumber)
      logger.info("SMS Provider providerType: " + Config.current.provider.providerType)
      logger.info("SMS Provider provisionNumbers: " + Config.current.provider.provisionNumbers)
      logger.info("SMS Provider smsStatusCallback: " + Config.current.provider.smsStatusCallback)
      logger.info("SMS Provider smsURL: " + Config.current.provider.smsURL)
      logger.info("SMS Provider socketTimeoutInMillis: " + Config.current.provider.socketTimeoutInMillis)

      val healthService = new HealthCheckService(applicationName, applicationVersion)
                            .registerCheck(RepoManager.healthCheck)
                            .registerCheck(smsGateway.connectionHealthCheck)

      server.register(new StatusController(applicationName))
      server.register(new HealthCheckController(service = healthService))
      server.register(new SubscriptionController(smsService))
      server.register(new TwilioController(smsService))
      logger.info("SERVER: Registered SubscriptionController.")
      logger.info("SERVER: Starting ...")
      server.main(args)

    } finally {
      executor.shutdownNow()
    }
  }
}
