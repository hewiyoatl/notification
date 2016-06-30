package com.loyal3.sms.service.provider.twilio

import com.loyal3.sms.service.provider.{TwilioSendResponse, SendRequest, SendResponse}
import com.twilio.sdk.TwilioRestClient
import org.mockito.Mock
import scala.concurrent.Await
import scala.concurrent.duration._
import com.loyal3.sms.test.support.scopes.SelfAwareSpecification
import org.specs2.mock.Mockito
import com.loyal3.sms.config.Config
import com.loyal3.sms.core.Subscription
import java.util.UUID
import scala.util.{Try, Success, Failure}

class TwilioProviderIntegrationSpec extends SelfAwareSpecification with Mockito  {

  //TechOps test account
  val accountSid: String = "AC387f15c74e900353826a29a69721292a"
  val authToken: String = "b8cba54896356911a77de58e61c6d99b"

  //From
  val invalidPhone: String = "15005550001" //error code: 21212
  val validPhone: String = "+15005550006"
  val nonSmsCapablePhone: String = "15005550007" //error code: 21606
  val fullSmsQueuePhone: String = "15005550008" //error code: 21611

  val areaCode: String = "415"

  val subsId: String = UUID.randomUUID.toString

  def updateMessages(resp: TwilioSendResponse): Unit = {}

  "#send" should {
    "send a message to twilio" in {

      // Test credentials
      val twilio = new TwilioProvider(accountSid = accountSid,
          authToken = authToken)

      // When
      val response: SendResponse = Await.result(twilio.send(SendRequest(subsId, validPhone, "+14156963449", "blahMessage"), updateMessages), Duration(24, HOURS))

      // Then
      response.status mustEqual  ("queued")
      response.code mustEqual null
    }

    "send a message fail: invalid number" in {

      // Test credentials
      val twilio = new TwilioProvider(accountSid = accountSid,
        authToken = authToken)

      // When
      val response: SendResponse = Await.result(twilio.send(SendRequest(subsId, validPhone, "+15005550001", "blahMessage"), updateMessages), Duration(24, HOURS))

      // Then
      response.status mustEqual  ("400")
      response.code mustEqual ("21211")
    }

    "send a message fail: cannot route" in {

      // Test credentials
      val twilio = new TwilioProvider(accountSid = accountSid,
        authToken = authToken)

      // When
      val response: SendResponse = Await.result(twilio.send(SendRequest(subsId, validPhone, "+15005550002", "blahMessage"), updateMessages), Duration(24, HOURS))

      // Then
      response.status mustEqual  ("400")
      response.code mustEqual ("21612")
    }

    "send a message fail: no international permissions" in {

      // Test credentials
      val twilio = new TwilioProvider(accountSid = accountSid,
        authToken = authToken)

      // When
      val response: SendResponse = Await.result(twilio.send(SendRequest(subsId, validPhone, "+15005550003", "blahMessage"), updateMessages), Duration(24, HOURS))

      // Then
      response.status mustEqual  ("400")
      response.code mustEqual ("21408")
    }

    "send a message fail: number is blacklisted" in {

      // Test credentials
      val twilio = new TwilioProvider(accountSid = accountSid,
        authToken = authToken)

      // When
      val response: SendResponse = Await.result(twilio.send(SendRequest(subsId, validPhone, "+15005550004", "blahMessage"), updateMessages), Duration(24, HOURS))

      // Then
      response.status mustEqual  ("400")
      response.code mustEqual ("21610") //@see https://www.twilio.com/docs/errors/21610
    }

    "send a message fail: From number is invalid" in {

      // Test credentials
      val twilio = new TwilioProvider(accountSid = accountSid,
        authToken = authToken)

      // When
      val response: SendResponse = Await.result(twilio.send(SendRequest(subsId, invalidPhone, "+14156963449", "blahMessage"), updateMessages), Duration(24, HOURS))

      // Then
      response.status mustEqual  ("400")
      response.code mustEqual ("21212")
    }

    "purchase number with provider false" in {

      if (Config.isDevEnvironment) {
        // Test credentials
        val twilio = new TwilioProvider(accountSid = accountSid,
          authToken = authToken)

        // When
        val response = twilio.getPhoneNumber(Subscription(topic = "ipo"))

        // Then
        response mustNotEqual("")
      }
      else {
        true mustEqual true
      }
    }

    "buyPhoneNumber should return None if all area code is not available " in {
      if (Config.isDevEnvironment) {
        val twilio = new TwilioProvider(accountSid = accountSid,
          authToken = authToken){
          override  def buyPhoneNumberFromAreaCode(oneAreaCode: String): Option[String] = {
            return None
          }

          def runtest(): Option[String] ={
            this.buyPhoneNumber
          }
        }

        twilio.runtest() mustEqual None
      }
      else {
        true mustEqual true
      }

    }

    "buyPhoneNumber should return a valid phone number on one available area code " in {
      val validPhoneNumber = Some("123456789")
      if (Config.isDevEnvironment) {
        val twilio = new TwilioProvider(accountSid = accountSid,
          authToken = authToken){
          override  def buyPhoneNumberFromAreaCode(oneAreaCode: String): Option[String] = {
            oneAreaCode match{
              case "415" => None
              case "650" => None
              case "510" => validPhoneNumber
            }
          }

          def runtest(): Option[String] ={
            this.buyPhoneNumber
          }
        }

        twilio.runtest() mustEqual validPhoneNumber
      }
      else {
        true mustEqual true
      }

    }

  }

  "#healthCheck" should {

    "healthCheck" in {

      // Test credentials
      val twilio = new TwilioProvider(accountSid = accountSid,
        authToken = authToken)

      if (Config.isDevEnvironment) {

        var message = ""

        // When
        Try(twilio.healthCheck) match {
          case Success(s) => message = ""
          case Failure(ex) => message = ex.getMessage
        }

        // Then
        message mustNotEqual("")
      }
      else {

        val actualResponse = twilio.healthCheck
        actualResponse mustEqual (true)
      }
    }

    "healthCheckTwilioSMS" in {

      // Test credentials
      val twilio = new TwilioProvider(accountSid = accountSid,
        authToken = authToken)

      if (Config.isDevEnvironment) {

        var message = ""
        var response = false

        // When
        Try(response = twilio.healthCheckTwilioSMS) match {
          case Success(s) => message = ""
          case Failure(ex) => message = ex.getMessage
        }

        // Then
        message mustEqual("")
        response mustEqual(true)
      }
      else {

        val actualResponse = twilio.healthCheckTwilioSMS
        actualResponse mustEqual (true)
      }
    }
  }
}
