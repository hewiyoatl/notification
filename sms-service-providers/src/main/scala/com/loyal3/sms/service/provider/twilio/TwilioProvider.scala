package com.loyal3.sms.service.provider.twilio

import java.util

import com.loyal3.service.health.{HealthCheckResult, Healthy, Warning}
import com.loyal3.sms.service.provider._
import com.twilio.sdk.{TwilioRestClient, TwilioRestException, TwilioUtils}
import org.apache.http.impl.conn.PoolingClientConnectionManager
import org.apache.http.message.BasicNameValuePair
import org.apache.http.pool.PoolStats
import org.apache.http.{HttpVersion, NameValuePair}
import org.apache.log4j.Logger

import scala.collection.mutable.ListBuffer
import scala.collection.JavaConversions._
import com.loyal3.sms.service.provider.TwilioSendResponse
import com.loyal3.sms.service.provider.SendRequest

import scala.io.Source
import java.util.concurrent.Executors

import scala.concurrent.duration._
import scala.concurrent._
import org.apache.http.impl.client.DefaultHttpClient
import com.loyal3.sms.config.Config
import com.twilio.sdk.resource.factory.SmsFactory
import com.twilio.sdk.resource.instance.Sms
import com.twilio.sdk.resource.list.AvailablePhoneNumberList
import com.loyal3.sms.service.repository.RepoManager
import com.loyal3.sms.core.{Subscription, SubscriptionState}
import java.util.Date

/**
  * SMS provider implementation for <a href="https://www.twilio.com/">Twilio</a>
  *
  * It depends on <a href="https://github.com/twilio/twilio-java">Twilio Java</a>
  */
class TwilioProvider(accountSid: String, authToken: String,
                     twilioClient: Option[TwilioRestClient] = None,
                     execContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global)
  extends SmsProvider {

  implicit val ec: ExecutionContext = execContext

  val logger: Logger = Logger.getLogger(getClass)

  val _twilioClient: TwilioRestClient = {

    //TODO update me to apache http client 4.3.1 way to move away from deprecated code
    //Currently twilio java client hardcoded the default max per route, copied from TwilioRestClient.java
    val mgr: PoolingClientConnectionManager = new PoolingClientConnectionManager
    mgr.setDefaultMaxPerRoute(Config.current.provider.maxConnectionPerRoute) //externalize me
    mgr.setMaxTotal(Config.current.provider.maxConnectionPerRoute) //max connection should be inline with the number of threads maximizing throughput

    val httpclient = new DefaultHttpClient(mgr)
    httpclient.getParams().setParameter("http.protocol.version", HttpVersion.HTTP_1_1)
    httpclient.getParams().setParameter("http.socket.timeout", Config.current.provider.socketTimeoutInMillis)
    httpclient.getParams().setParameter("http.connection.timeout", Config.current.provider.connectionTimeoutInMillis)
    httpclient.getParams().setParameter("http.protocol.content-charset", "UTF-8")

    val client = twilioClient.getOrElse(new TwilioRestClient(accountSid, authToken))
    client.setHttpClient(httpclient)
    client
  }

  val smsFactory: SmsFactory = _twilioClient.getAccount.getSmsFactory

  /**
    * Sends SMS message to a phoneNumber
    * returns a sendResponse of the send status or error code if fail
    */
  def send(sendRequest: SendRequest, callback: (TwilioSendResponse) => Unit): Future[SendResponse] = {

    return Future {
      val messageParams: java.util.List[NameValuePair] = List(
        new BasicNameValuePair("From", sendRequest.fromPhone),
        new BasicNameValuePair("To", sendRequest.phoneNumber),
        new BasicNameValuePair("Body", sendRequest.message),
        new BasicNameValuePair("StatusCallback", Config.current.provider.smsStatusCallback))

      try {
        val sms: Sms = smsFactory.create(messageParams)
        val res = TwilioSendResponse(sendRequest.subsId, null, sms)
        logger.info(this.getClass + " Timestamp: |%s| - SMS_API res.toString: %s, Status: %s, SId: %s, status: %s".format((new Date()).toString, res.toString, res.status, res.sms.getSid, res.sms.getStatus))
        callback(res)
        res
      }
      catch {
        case e: TwilioRestException => {
          logger.error(this.getClass + " Error code :%s, From: %s, To: %s, Body :%s, error message: %s, Exception: %s".format(e.getErrorCode.toString, sendRequest.fromPhone, sendRequest.phoneNumber, sendRequest.message, e.getErrorMessage, e.getMessage))
          SimpleSendResponse("400", e.getErrorCode.toString)
        }
      }
    }
  }

  def getPhoneNumber(subscription: Subscription): Option[String] = synchronized({
    if (Config.current.provider.provisionNumbers) {
      LongCodeMapper.existingLongCode(subscription.topic) match {
        case lc if (lc.isDefined && lc.get.count < (Config.current.provider.maxUsersPerNumber + 1)) => {
          Some(lc.get.longCode)
        }
        case _ => {
          val phoneNumberOpt: Option[String] = buyPhoneNumber
          logger.info("buyPhoneNumber=" + phoneNumberOpt.getOrElse("None"))

          phoneNumberOpt match {
            case Some(phoneNumber) =>
              LongCodeMapper.addOfferId(subscription.topic, phoneNumber)

              Some(phoneNumber)
            case None =>
              None
          }
        }
      }
    }
    else
      Some(Config.current.provider.fromPhoneNumber)
  })

  def releasePhoneNumber(phoneNumber: String): Boolean =
    try {
      _twilioClient
        .getAccount
        .getIncomingPhoneNumbers(Map("PhoneNumber" -> s"*$phoneNumber"))
        .map(
          number => {
            val deleted = number.delete()
            if (!deleted)
              logger.error(s"Failed deletion of $phoneNumber")
            deleted
          }).fold(false)((z, b) => z || b)
    } catch {
      case e: TwilioRestException =>
        logger.error(s"Error releasing longCode: ${e.getMessage}\n ${e.getStackTraceString}")
        false
    }


  protected def buyPhoneNumber: Option[String] = {
    val areaCodeList = Config.current.provider.areaCode.split(",")
    for (i <- 0 to 3) {
      for (oneAreaCode <- areaCodeList) {
        val phoneNumberOpt = buyPhoneNumberFromAreaCode(oneAreaCode.trim)
        phoneNumberOpt match {
          case Some(phoneNumer) => return phoneNumberOpt
          case _ => // do nothing, try next one
        }
      }
    }
    logger.error(s"buyPhoneNumber failed after retrying area codes=${Config.current.provider.areaCode}", new Exception(""))
    None
  }

  protected def buyPhoneNumberFromAreaCode(oneAreaCode: String): Option[String] = {
    // Find available numbers for given area code
    val availableNumbers: AvailablePhoneNumberList = _twilioClient.getAccount.
      getAvailablePhoneNumbers(Map("AreaCode" -> oneAreaCode), "US", "Local")

    val availablePhoneNumberList = availableNumbers.getPageData
    if (availablePhoneNumberList.size() > 0) {
      val purchaseParams: util.List[NameValuePair] = List(
        new BasicNameValuePair("AreaCode", oneAreaCode),
        new BasicNameValuePair("SmsMethod", "POST"),
        new BasicNameValuePair("SmsUrl", Config.current.provider.smsURL)
      )
      try {
        val response = _twilioClient.getAccount.getIncomingPhoneNumberFactory.create(purchaseParams)
        val phoneNumber = response.getPhoneNumber
        logger.info(s"buyPhoneNumberFromAreaCode with area code=${oneAreaCode}, get a phone number=${phoneNumber}")
        return Some(phoneNumber)
      }
      catch {
        case e: Exception =>
          logger.error(this.getClass + s"buyPhoneNumberFromAreaCode failed to purchasePhoneNumber from areacode: ${oneAreaCode} ", e)
      }
    }
    else {
      logger.warn(this.getClass + s"buyPhoneNumberFromAreaCode no phone numbers available in areacode: ${oneAreaCode}")
    }
    None
  }

  val twilioUtils = new TwilioUtils(Config.current.provider.authToken)

  def validateRequest(signature: String, url: String, params: Map[String, String]) =
    if (!twilioUtils.validateRequest(signature, url, params)) throw new SecurityException("Failed to validate Twilio signature")

  /**
    * This functions validates connectivity to twilio and actually sends a message to a given phone number
    */
  def healthCheckTwilioSMS: Boolean = {
    val messageParams: java.util.List[NameValuePair] = List(
      new BasicNameValuePair("From", Config.current.provider.fromPhoneNumber),
      new BasicNameValuePair("To", Config.current.provider.healthCheckPhoneNumber),
      new BasicNameValuePair("Body", Config.current.provider.healthCheckMessage))

    val sms: Sms = smsFactory.create(messageParams)
    val res = TwilioSendResponse("", null, sms)
    res.sms.getAccountSid != null
  }

  /**
    * This functions validates connectivity with twilio.
    */
  def healthCheck: Boolean = {
    val token = _twilioClient.getAccount.getApplications
    token != null && !token.isEmpty
  }

  /**
    * This functions validates connection pool status.
    */
  def healthCheckPool: HealthCheckResult = {
    val poolingStats = _twilioClient.getHttpClient.getConnectionManager.asInstanceOf[PoolingClientConnectionManager].getTotalStats
    poolingStats match {
      case ok: PoolStats if ok.getPending == 0 => Healthy()
      case warn: PoolStats => Warning(s"Pool to service is at capacity: ${warn.toString}")
    }
  }

}

object TwilioProvider {
  //class wide methods go here
}


object Main {

  def main(args: Array[String]): Unit = {
    //Hewi/ Kevin: setting same number of threads to same number of connections to Twilio ensuring no connection timeout
    val executor = Executors.newFixedThreadPool(Config.current.provider.maxConnectionPerRoute)
    try {
      implicit val ec: ExecutionContext = ExecutionContext.fromExecutorService(executor)

      val subscriptionRepository = RepoManager.subscriptionRepository

      args match {


        // COMMAND 1: Send a message to a batch of phones
        case Array(accountSid, authToken, command, fromNumber, message, csvFileName) if command == "batchsend" =>

          val provider: TwilioProvider = new TwilioProvider(accountSid, authToken, execContext = ec)

          val csvFile = new java.io.File(csvFileName)
          if (!csvFile.exists() || !csvFile.canRead) {
            sys.error("Unable to read csv file: " + csvFile)
            sys.exit(1)
          }
          val source = Source.fromFile(csvFile)

          val futureResponseList: ListBuffer[Future[SendResponse]] = new ListBuffer[Future[SendResponse]]
          val startTime = System.currentTimeMillis()
          var count = 0
          try {
            source.getLines foreach {
              case line: String => {
                val phoneNumber: String = line.trim
                subscriptionRepository.findByPhoneNumber(phoneNumber) match {
                  case Seq() => {
                    val request = SendRequest(subsId = "",
                      fromPhone = fromNumber,
                      phoneNumber = phoneNumber,
                      message = message)

                    try {
                      val response: Future[SendResponse] = provider.send(request, _ => ())
                      futureResponseList += response
                      count = count + 1
                      print(".")
                    }
                    catch {
                      case e: Exception => sys.error(line + ", msg: " + e.getMessage)
                    }
                  }

                  case _ => {
                  }
                }

                if (count % 100 == 0) {
                  println("\nSent %s message(s), time: %s seconds".format(count, (System.currentTimeMillis() - startTime) / 1000L))
                }
              }
            }

            println("\nSent %s message(s)".format(count))
            println("Completed in %s seconds".format((System.currentTimeMillis() - startTime) / 1000L))

            val agg = Future.sequence(futureResponseList)
            val responseList = Await.result(agg, 12.hours)
            println("list size: " + responseList.length)
            responseList foreach {
              println(_)
            }
          }
          finally {
            source.close()
            println("Done")
            println("Completed in %s seconds".format((System.currentTimeMillis() - startTime) / 1000L))
          }

        // COMMAND 2: Release al long code
        case Array(accountSid, authToken, command, number) if command == "release" =>

          val provider: TwilioProvider = new TwilioProvider(accountSid, authToken, execContext = ec)


          println(s"Release twilio number  $number ")

          val released = provider.releasePhoneNumber(number)
          println(s"Release twilio number ${if (released) "DONE" else "FAILED"}")

        case _ =>
          println("Usage\n<Twilio account sid> <Twilio auth token> batchsend <Twilio from number> <message> <path to csv file>\n<Twilio account sid> <Twilio auth token> release <Twilio from number>")
          println("csv file should contain one phone number per line")
          sys.exit(1)
      }

    } finally {
      executor.shutdownNow()
    }
  }

}
