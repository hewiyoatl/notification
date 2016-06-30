package com.loyal3.sms.service.provider.twilio

import com.loyal3.sms.test.support.scopes.SelfAwareSpecification
import com.loyal3.sms.service.controller.FinatraHelpers
import com.loyal3.sms.service.controller.twilio.TwilioController
import org.scalatest.mock.MockitoSugar
import com.loyal3.sms.service.SmsService
import org.mockito.Mockito._
import com.loyal3.sms.core.util.ResourceBundles
import com.loyal3.sms.core.{SubscriptionBuilder,SmsServiceResponse,IncomingSMSResponse,Subscription,IncomingSMS}
import java.util.{Calendar, UUID}
import com.loyal3.sms.core.validators.PhoneNumberValidation
import com.loyal3.sms.service.repository.{RequestRepository, SubscriptionRepository}
import java.sql.Timestamp
import org.apache.commons.lang.StringEscapeUtils

class TwilioControllerSpec extends SelfAwareSpecification with FinatraHelpers with MockitoSugar{

  "POST /api/sms/callback/twilio/incoming" should {

    "receive user response START (lowercase) from Twilio" in {

      val smsService: SmsService = mock[SmsService]
      val subscriptionRepository: SubscriptionRepository = mock[SubscriptionRepository]
      val app = usingController(new TwilioController(smsService))
      val longCode = UUID.randomUUID.toString
      val offerName = UUID.randomUUID.toString
      val params = Map("From" -> "5102234567", "Body" -> "start", "To" -> longCode)
      val subscription = SubscriptionBuilder(id = UUID.randomUUID.toString, longCode = params("To"), phoneNumber = params("From"), offerName = offerName).build

      //When
      when(smsService.verify(subscription)).thenReturn(SmsServiceResponse("ok"))
      when(smsService.messageType(longCode)).thenReturn(Some("SUBSCRIBE"))
      when(smsService.handleIncoming(subscription, msgBody = params("Body"))).thenReturn(IncomingSMSResponse("ok"))
      when(smsService.handleMessage(IncomingSMS(phoneNumber = params("From"), longCode = longCode, msgBody = params("Body").trim))).thenReturn(Option(subscription))
      when(subscriptionRepository.findByLongCodePhoneNumber(longCode, params("From"))).thenReturn(Option(subscription))

      //Then
      app.post("/api/sms/callback/twilio/incoming", params = params)

      val response = app.response

      //Verify
      response.code mustEqual 200
      response.body.contains(ResourceBundles.Messages.Verify.getString(offerName)) mustEqual true
    }

    "receive user response SUBSCRIBE (lowercase) from Twilio" in {

      val smsService: SmsService = mock[SmsService]
      val app = usingController(new TwilioController(smsService))
      val longCode = UUID.randomUUID.toString
      val offerName = UUID.randomUUID.toString
      val params = Map("From" -> "5102234567", "Body" -> "subscribe", "To" -> longCode)
      val subscription = SubscriptionBuilder(longCode = params("To"), phoneNumber = params("From"), offerName = offerName).build
      val subscriptionRepository: SubscriptionRepository = mock[SubscriptionRepository]

      //When
      when(smsService.verify(subscription)).thenReturn(SmsServiceResponse("ok"))
      when(smsService.messageType(longCode)).thenReturn(Some("SUBSCRIBE"))
      when(smsService.handleIncoming(subscription, msgBody = params("Body"))).thenReturn(IncomingSMSResponse("ok"))
      when(smsService.handleMessage(IncomingSMS(phoneNumber = params("From"), longCode = longCode, msgBody = params("Body").trim))).thenReturn(Option(subscription))
      when(subscriptionRepository.findByLongCodePhoneNumber(longCode, params("From"))).thenReturn(Option(subscription))

      //Then
      app.post("/api/sms/callback/twilio/incoming", params = params)

      val response = app.response

      //Verify
      response.code mustEqual 200
      response.body.contains(ResourceBundles.Messages.Verify.getString(offerName)) mustEqual true
    }

    "receive user response YES (uppercase) from Twilio" in {

      val smsService: SmsService = mock[SmsService]
      val app = usingController(new TwilioController(smsService))
      val longCode = UUID.randomUUID.toString
      val offerName = UUID.randomUUID.toString
      val params = Map("From" -> "5102234567", "Body" -> "YES", "To" -> longCode)
      val subscription = SubscriptionBuilder(longCode = params("To"), phoneNumber = params("From"), offerName = offerName).build
      val subscriptionRepository: SubscriptionRepository = mock[SubscriptionRepository]

      //When
      when(smsService.verify(subscription)).thenReturn(SmsServiceResponse("ok"))
      when(smsService.messageType(longCode)).thenReturn(Some("SUBSCRIBE"))
      when(smsService.handleIncoming(subscription, msgBody = params("Body"))).thenReturn(IncomingSMSResponse("ok"))
      when(smsService.handleMessage(IncomingSMS(phoneNumber = params("From"), longCode = longCode, msgBody = params("Body").trim))).thenReturn(Option(subscription))
      when(subscriptionRepository.findByLongCodePhoneNumber(longCode, params("From"))).thenReturn(Option(subscription))

      //Then
      app.post("/api/sms/callback/twilio/incoming", params = params)

      val response = app.response

      //Verify
      response.code mustEqual 200
      response.body.contains(ResourceBundles.Messages.Verify.getString(offerName)) mustEqual true
    }

    "receive user response YES (lowercase) from Twilio" in {

      val smsService: SmsService = mock[SmsService]
      val app = usingController(new TwilioController(smsService))
      val longCode = UUID.randomUUID.toString
      val offerName = UUID.randomUUID.toString
      val params = Map("From" -> "5102234567", "Body" -> "yes", "To" -> longCode)
      val subscription = SubscriptionBuilder(longCode = params("To"), phoneNumber = params("From"), offerName = offerName).build
      val subscriptionRepository: SubscriptionRepository = mock[SubscriptionRepository]

      //When
      when(smsService.verify(subscription)).thenReturn(SmsServiceResponse("ok"))
      when(smsService.messageType(longCode)).thenReturn(Some("SUBSCRIBE"))
      when(smsService.handleIncoming(subscription, msgBody = params("Body"))).thenReturn(IncomingSMSResponse("ok"))
      when(smsService.handleMessage(IncomingSMS(phoneNumber = params("From"), longCode = longCode, msgBody = params("Body").trim))).thenReturn(Option(subscription))
      when(subscriptionRepository.findByLongCodePhoneNumber(longCode, params("From"))).thenReturn(Option(subscription))

      //Then
      app.post("/api/sms/callback/twilio/incoming", params = params)

      val response = app.response

      //Verify
      response.code mustEqual 200
      response.body.contains(ResourceBundles.Messages.Verify.getString(offerName)) mustEqual true
    }

    "receive user response YES (mix case) from Twilio" in {

      val smsService: SmsService = mock[SmsService]
      val app = usingController(new TwilioController(smsService))
      val longCode = UUID.randomUUID.toString
      val offerName = UUID.randomUUID.toString
      val params = Map("From" -> "5102234567", "Body" -> "YeS", "To" -> longCode)
      val subscription = SubscriptionBuilder(longCode = params("To"), phoneNumber = params("From"), offerName = offerName).build
      val subscriptionRepository: SubscriptionRepository = mock[SubscriptionRepository]

      //When
      when(smsService.verify(subscription)).thenReturn(SmsServiceResponse("ok"))
      when(smsService.messageType(longCode)).thenReturn(Some("SUBSCRIBE"))
      when(smsService.handleIncoming(subscription, msgBody = params("Body"))).thenReturn(IncomingSMSResponse("ok"))
      when(smsService.handleMessage(IncomingSMS(phoneNumber = params("From"), longCode = longCode, msgBody = params("Body").trim))).thenReturn(Option(subscription))
      when(subscriptionRepository.findByLongCodePhoneNumber(longCode, params("From"))).thenReturn(Option(subscription))

      //Then
      app.post("/api/sms/callback/twilio/incoming", params = params)

      val response = app.response

      //Verify
      response.code mustEqual 200
      response.body.contains(ResourceBundles.Messages.Verify.getString(offerName)) mustEqual true
    }

    "receive user response YES with leading and trailing spaces from Twilio" in {

      val smsService: SmsService = mock[SmsService]
      val app = usingController(new TwilioController(smsService))
      val longCode = UUID.randomUUID.toString
      val offerName = UUID.randomUUID.toString
      val params = Map("From" -> "5102234567", "Body" -> "       Yes     ", "To" -> longCode)
      val subscription = SubscriptionBuilder(longCode = params("To"), phoneNumber = params("From"), offerName = offerName).build
      val subscriptionRepository: SubscriptionRepository = mock[SubscriptionRepository]

      //When
      when(smsService.verify(subscription)).thenReturn(SmsServiceResponse("ok"))
      when(smsService.messageType(longCode)).thenReturn(Some("SUBSCRIBE"))
      when(smsService.handleIncoming(subscription, msgBody = params("Body").trim)).thenReturn(IncomingSMSResponse("ok"))
      when(smsService.handleMessage(IncomingSMS(phoneNumber = params("From"), longCode = longCode, msgBody = params("Body").trim))).thenReturn(Option(subscription))
      when(subscriptionRepository.findByLongCodePhoneNumber(longCode, params("From"))).thenReturn(Option(subscription))

      //Then
      app.post("/api/sms/callback/twilio/incoming", params = params)

      val response = app.response

      //Verify
      response.code mustEqual 200
      response.body.contains(ResourceBundles.Messages.Verify.getString(offerName)) mustEqual true
    }

    "receive user response NO (uppercase) from Twilio" in {

      val smsService: SmsService = mock[SmsService]
      val app = usingController(new TwilioController(smsService))
      val longCode = UUID.randomUUID.toString
      val offerName = UUID.randomUUID.toString
      val params = Map("From" -> "5102234567", "Body" -> "NO", "To" -> longCode)
      val subscription = SubscriptionBuilder(phoneNumber = params("From"), offerName = offerName).build
      val subscriptionRepository: SubscriptionRepository = mock[SubscriptionRepository]

      //When
      when(smsService.verify(subscription)).thenReturn(SmsServiceResponse("ok"))
      when(smsService.messageType(longCode)).thenReturn(Some("SUBSCRIBE"))
      when(smsService.handleIncoming(subscription, msgBody = params("Body"))).thenReturn(IncomingSMSResponse("ok"))
      when(smsService.handleMessage(IncomingSMS(phoneNumber = params("From"), longCode = longCode, msgBody = params("Body").trim))).thenReturn(Option(subscription))
      when(subscriptionRepository.findByLongCodePhoneNumber(longCode, params("From"))).thenReturn(Option(subscription))

      app.post("/api/sms/callback/twilio/incoming", params = params)

      val response = app.response
      response.code mustEqual 200
      response.body.contains(ResourceBundles.Messages.Unsubscribe.getString(offerName)) mustEqual true
    }

    "receive user response NO (lowercase) from Twilio" in {

      val smsService: SmsService = mock[SmsService]

      val app = usingController(new TwilioController(smsService))
      val longCode = UUID.randomUUID.toString
      val offerName = UUID.randomUUID.toString
      val params = Map("From" -> "5102234567", "Body" -> "no", "To" -> longCode)
      val subscription = SubscriptionBuilder(phoneNumber = params("From"), offerName = offerName).build
      val subscriptionRepository: SubscriptionRepository = mock[SubscriptionRepository]

      //When
      when(smsService.verify(subscription)).thenReturn(SmsServiceResponse("ok"))
      when(smsService.messageType(longCode)).thenReturn(Some("SUBSCRIBE"))
      when(smsService.handleIncoming(subscription, msgBody = params("Body"))).thenReturn(IncomingSMSResponse("ok"))
      when(smsService.handleMessage(IncomingSMS(phoneNumber = params("From"), longCode = longCode, msgBody = params("Body").trim))).thenReturn(Option(subscription))
      when(subscriptionRepository.findByLongCodePhoneNumber(longCode, params("From"))).thenReturn(Option(subscription))

      app.post("/api/sms/callback/twilio/incoming", params = params)

      val response = app.response
      response.code mustEqual 200
      response.body.contains(ResourceBundles.Messages.Unsubscribe.getString(offerName)) mustEqual true
    }

    "receive user response UNSUBSCRIBE (lowercase) from Twilio" in {

      val smsService: SmsService = mock[SmsService]
      val longCode = UUID.randomUUID.toString
      val offerName = UUID.randomUUID.toString
      val params = Map("From" -> "5102234567", "Body" -> "unsubscribe", "To" -> longCode)

      val app = usingController(new TwilioController(smsService))
      val subscription = SubscriptionBuilder(phoneNumber = params("From"), offerName = offerName).build
      val subscriptionRepository: SubscriptionRepository = mock[SubscriptionRepository]

      //When
      when(smsService.verify(subscription)).thenReturn(SmsServiceResponse("ok"))
      when(smsService.messageType(longCode)).thenReturn(Some("SUBSCRIBE"))
      when(smsService.handleIncoming(subscription, msgBody = params("Body"))).thenReturn(IncomingSMSResponse("ok"))
      when(smsService.handleMessage(IncomingSMS(phoneNumber = params("From"), longCode = longCode, msgBody = params("Body").trim))).thenReturn(Option(subscription))
      when(subscriptionRepository.findByLongCodePhoneNumber(longCode, params("From"))).thenReturn(Option(subscription))

      app.post("/api/sms/callback/twilio/incoming", params = params)

      val response = app.response
      response.code mustEqual 200
      response.body.contains(ResourceBundles.Messages.Unsubscribe.getString(offerName)) mustEqual true
    }

    "receive user response STOP (lowercase) from Twilio" in {

      val smsService: SmsService = mock[SmsService]
      val longCode = UUID.randomUUID.toString
      val offerName = UUID.randomUUID.toString
      val params = Map("From" -> "5102234567", "Body" -> "stop", "To" -> longCode)
      val subscriptionRepository: SubscriptionRepository = mock[SubscriptionRepository]

      val app = usingController(new TwilioController(smsService))
      val subscription = SubscriptionBuilder(phoneNumber = params("From"), offerName = offerName).build

      //When
      when(smsService.verify(subscription)).thenReturn(SmsServiceResponse("ok"))
      when(smsService.messageType(longCode)).thenReturn(Some("SUBSCRIBE"))
      when(smsService.handleIncoming(subscription, msgBody = params("Body"))).thenReturn(IncomingSMSResponse("ok"))
      when(smsService.handleMessage(IncomingSMS(phoneNumber = params("From"), longCode = longCode, msgBody = params("Body").trim))).thenReturn(Option(subscription))
      when(subscriptionRepository.findByLongCodePhoneNumber(longCode, params("From"))).thenReturn(Option(subscription))

      app.post("/api/sms/callback/twilio/incoming", params = params)

      val response = app.response
      response.code mustEqual 200
      response.body.contains(ResourceBundles.Messages.Unsubscribe.getString(offerName)) mustEqual true
    }

    "receive user response NO (mix case) from Twilio" in {

      val smsService: SmsService = mock[SmsService]
      val longCode = UUID.randomUUID.toString
      val offerName = UUID.randomUUID.toString
      val params = Map("From" -> "5102234567", "Body" -> "No", "To" -> longCode)
      val subscriptionRepository: SubscriptionRepository = mock[SubscriptionRepository]

      val app = usingController(new TwilioController(smsService))
      val subscription = SubscriptionBuilder(phoneNumber = params("From"), offerName = offerName).build

      //When
      when(smsService.verify(subscription)).thenReturn(SmsServiceResponse("ok"))
      when(smsService.messageType(longCode)).thenReturn(Some("SUBSCRIBE"))
      when(smsService.handleIncoming(subscription, msgBody = params("Body"))).thenReturn(IncomingSMSResponse("ok"))
      when(smsService.handleMessage(IncomingSMS(phoneNumber = params("From"), longCode = longCode, msgBody = params("Body").trim))).thenReturn(Option(subscription))
      when(subscriptionRepository.findByLongCodePhoneNumber(longCode, params("From"))).thenReturn(Option(subscription))

      app.post("/api/sms/callback/twilio/incoming", params = params)

      val response = app.response
      response.code mustEqual 200
      response.body.contains(ResourceBundles.Messages.Unsubscribe.getString(offerName)) mustEqual true
    }

    "receive user response NO with leading and trailing spaces from Twilio" in {

      val smsService: SmsService = mock[SmsService]
      val longCode = UUID.randomUUID.toString
      val offerName = UUID.randomUUID.toString
      val params = Map("From" -> "5102234567", "Body" -> "    No       ", "To" -> longCode)
      val subscription = SubscriptionBuilder(phoneNumber = params("From"), offerName = offerName).build
      val subscriptionRepository: SubscriptionRepository = mock[SubscriptionRepository]

      //When
      when(smsService.verify(subscription)).thenReturn(SmsServiceResponse("ok"))
      when(smsService.messageType(longCode)).thenReturn(Some("SUBSCRIBE"))
      when(smsService.handleIncoming(subscription, msgBody = params("Body").trim)).thenReturn(IncomingSMSResponse("ok"))
      when(smsService.handleMessage(IncomingSMS(phoneNumber = params("From"), longCode = longCode, msgBody = params("Body").trim))).thenReturn(Option(subscription))
      when(subscriptionRepository.findByLongCodePhoneNumber(longCode, params("From"))).thenReturn(Option(subscription))

      val app = usingController(new TwilioController(smsService))
      app.post("/api/sms/callback/twilio/incoming", params = params)

      val response = app.response
      response.code mustEqual 200
      response.body.contains(ResourceBundles.Messages.Unsubscribe.getString(offerName)) mustEqual true
    }

    "receive user response BOGUS from Twilio" in {

      val smsService: SmsService = mock[SmsService]
      val longCode = UUID.randomUUID.toString
      val params = Map("From" -> "5102234567", "Body" -> "BOGUS", "To" -> longCode)
      val subscription = SubscriptionBuilder(phoneNumber = params("From"), longCode = params("To")).build

      //When
      when(smsService.verify(subscription)).thenReturn(SmsServiceResponse("ok"))
      when(smsService.messageType(longCode)).thenReturn(Some("SUBSCRIBE"))
      when(smsService.handleIncoming(subscription, msgBody = params("Body"))).thenReturn(IncomingSMSResponse("ok"))

      val app = usingController(new TwilioController(smsService))

      app.post("/api/sms/callback/twilio/incoming", params = params)

      val response = app.response
      response.code mustEqual 200
      response.body.contains(ResourceBundles.Messages.SubscribeInvalid.getString) mustEqual true
    }

    "receive user response EMPTY_STRING from Twilio" in {

      val smsService: SmsService = mock[SmsService]
      val longCode = UUID.randomUUID.toString
      val params = Map("From" -> "5102234567", "Body" -> "", "To" -> longCode)
      val subscription = SubscriptionBuilder(phoneNumber = params("From"), longCode = params("To")).build

      //When
      when(smsService.verify(subscription)).thenReturn(SmsServiceResponse("ok"))
      when(smsService.messageType(longCode)).thenReturn(Some("SUBSCRIBE"))
      when(smsService.handleIncoming(subscription, msgBody = params("Body"))).thenReturn(IncomingSMSResponse("ok"))

      val app = usingController(new TwilioController(smsService))
      app.post("/api/sms/callback/twilio/incoming", params = params)

      val response = app.response
      response.code mustEqual 200
      response.body.contains(ResourceBundles.Messages.SubscribeInvalid.getString) mustEqual true
    }

    "receive user response missing Body parameter from Twilio" in {

      val smsService: SmsService = mock[SmsService]

      val app = usingController(new TwilioController(smsService))
      app.post("/api/sms/callback/twilio/incoming", params = Map("From" -> "5102234567"))

      val response = app.response
      response.code mustEqual 200
      response.body.contains(ResourceBundles.Messages.ErrorMissingParam.getString("Body")) mustEqual true
    }

    "receive user response empty From parameter from Twilio" in {

      val smsService: SmsService = mock[SmsService]
      val app = usingController(new TwilioController(smsService))
      val params = Map("From" -> "", "Body" -> "Yes")

      //When
      when(smsService.verify(SubscriptionBuilder(phoneNumber = params("From")).build)).thenReturn(SmsServiceResponse("ok"))

      //Then
      app.post("/api/sms/callback/twilio/incoming", params = params)

      val response = app.response

      //Verify
      response.code mustEqual 404
      response.body.contains("Invalid phone number") mustEqual true
    }

    "receive user response missing From parameter from Twilio" in {

      val smsService: SmsService = mock[SmsService]

      val app = usingController(new TwilioController(smsService))
      app.post("/api/sms/callback/twilio/incoming", params = Map("Body" -> "Yes"))

      val response = app.response
      response.code mustEqual 200
      response.body.contains(ResourceBundles.Messages.ErrorMissingParam.getString("From")) mustEqual true
    }

    "receive user response missing all parameters from Twilio" in {

      val smsService: SmsService = mock[SmsService]

      val app = usingController(new TwilioController(smsService))
      app.post("/api/sms/callback/twilio/incoming", params = Map())

      val response = app.response
      response.code mustEqual 200
      response.body.contains(ResourceBundles.Messages.ErrorMissingParam.getString("From") +
        ResourceBundles.Messages.ErrorMissingParam.getString("Body")) mustEqual true
    }

    "receive user response confirmation with valid parameters from Twilio" in {

      val smsService: SmsService = mock[SmsService]
      val longCode = UUID.randomUUID.toString
      val ipoName = "Test Org"
      val params = Map("From" -> "5102234567", "Body" -> "confirm", "To" -> longCode)
      val subscription = Subscription(phoneNumber = params("From"), longCode = params("To"), offerName = ipoName)
      val subscriptionRepository: SubscriptionRepository = mock[SubscriptionRepository]

      when(smsService.handleIncoming(subscription, msgBody = params("Body"))).thenReturn(IncomingSMSResponse(code = "ok"))
      when(smsService.messageType(longCode)).thenReturn(Some("CONFIRM"))
      when(smsService.handleIncoming(subscription, msgBody = params("Body"))).thenReturn(IncomingSMSResponse("ok"))
      when(smsService.handleMessage(IncomingSMS(phoneNumber = params("From"), longCode = longCode, msgBody = params("Body").trim))).thenReturn(Option(subscription))
      when(subscriptionRepository.findByLongCodePhoneNumber(longCode, params("From"))).thenReturn(Option(subscription))

      val app = usingController(new TwilioController(smsService))
      app.post("/api/sms/callback/twilio/incoming", params = params)

      val response = app.response
      response.code mustEqual 200
      response.body.contains(ResourceBundles.Messages.Confirm.getString(ipoName)) mustEqual true
    }

    "receive user incoming response has trailing spaces from Twilio" in {

      val smsService: SmsService = mock[SmsService]
      val longCode = UUID.randomUUID.toString
      val ipoName = "Test Org"
      val params = Map("From" -> "5102234567", "Body" -> " confirm ", "To" -> longCode)
      val subscription = Subscription(phoneNumber = params("From"), longCode = params("To"), offerName = ipoName)
      val subscriptionRepository: SubscriptionRepository = mock[SubscriptionRepository]

      when(smsService.handleIncoming(subscription, msgBody = params("Body"))).thenReturn(IncomingSMSResponse(code = "ok"))
      when(smsService.messageType(longCode)).thenReturn(Some("CONFIRM"))
      when(smsService.handleIncoming(subscription, msgBody = params("Body"))).thenReturn(IncomingSMSResponse("ok"))
      when(smsService.handleMessage(IncomingSMS(phoneNumber = params("From"), longCode = longCode, msgBody = params("Body").trim))).thenReturn(Option(subscription))
      when(subscriptionRepository.findByLongCodePhoneNumber(longCode, params("From"))).thenReturn(Option(subscription))

      val app = usingController(new TwilioController(smsService))
      app.post("/api/sms/callback/twilio/incoming", params = params)

      val response = app.response
      response.code mustEqual 200
      response.body.contains(ResourceBundles.Messages.Confirm.getString(ipoName)) mustEqual true
    }

    "receive user incoming response has upper case from Twilio" in {

      val smsService: SmsService = mock[SmsService]
      val longCode = UUID.randomUUID.toString
      val ipoName = "Test Org"
      val params = Map("From" -> "5102234567", "Body" -> " Confirm ", "To" -> longCode)
      val subscription = Subscription(phoneNumber = params("From"), longCode = params("To"), offerName = ipoName)
      val subscriptionRepository: SubscriptionRepository = mock[SubscriptionRepository]

      when(smsService.handleIncoming(subscription, msgBody = params("Body"))).thenReturn(IncomingSMSResponse(code = "ok"))
      when(smsService.messageType(longCode)).thenReturn(Some("CONFIRM"))
      when(smsService.handleIncoming(subscription, msgBody = params("Body"))).thenReturn(IncomingSMSResponse("ok"))
      when(smsService.handleMessage(IncomingSMS(phoneNumber = params("From"), longCode = longCode, msgBody = params("Body").trim))).thenReturn(Option(subscription))
      when(subscriptionRepository.findByLongCodePhoneNumber(longCode, params("From"))).thenReturn(Option(subscription))

      val app = usingController(new TwilioController(smsService))
      app.post("/api/sms/callback/twilio/incoming", params = params)

      val response = app.response
      response.code mustEqual 200
      response.body.contains(ResourceBundles.Messages.Confirm.getString(ipoName)) mustEqual true
    }

    "receive user incoming response has an invalid phone number from Twilio" in {

      val smsService: SmsService = mock[SmsService]
      val longCode = UUID.randomUUID.toString
      val params = Map("From" -> "", "Body" -> " Confirm ", "To" -> longCode)
      val incoming = IncomingSMS(phoneNumber = params("From"))
      val subscription = Subscription(phoneNumber = params("From"), longCode = params("To"))

      when(smsService.handleIncoming(subscription, msgBody = params("Body"))).thenReturn(IncomingSMSResponse(code = "failed"))

      val app = usingController(new TwilioController(smsService))
      app.post("/api/sms/callback/twilio/incoming", params = params)

      val response = app.response
      response.code mustEqual 404
      response.body.contains("Invalid phone number") mustEqual true
    }
  }

  "receive user response START (lowercase) from Twilio, with PLUS COUNTRY CODE" in {

    val smsService: SmsService = mock[SmsService]
    val app = usingController(new TwilioController(smsService))
    val longCode = UUID.randomUUID.toString
    val offerName = UUID.randomUUID.toString
    val params = Map("From" -> "+15102234567", "Body" -> "start", "To" -> longCode) // PLUS COUNTRY CODE
    val phoneNumberValidator = new PhoneNumberValidation {}
    val formatPhoneNumber = phoneNumberValidator.getFormattedPhoneNumber(params("From"))
    val subscription = SubscriptionBuilder(phoneNumber = formatPhoneNumber, offerName = offerName).build
    val subscriptionRepository: SubscriptionRepository = mock[SubscriptionRepository]

    //When
    when(smsService.verify(subscription)).thenReturn(SmsServiceResponse("ok"))
    when(smsService.messageType(longCode)).thenReturn(Some("SUBSCRIBE"))
    when(smsService.handleIncoming(subscription, msgBody = params("Body").trim)).thenReturn(IncomingSMSResponse("ok"))
    when(smsService.handleMessage(IncomingSMS(phoneNumber = formatPhoneNumber, longCode = longCode, msgBody = params("Body").trim))).thenReturn(Option(subscription))
    when(subscriptionRepository.findByLongCodePhoneNumber(longCode, params("From"))).thenReturn(Option(subscription))

    //Then
    app.post("/api/sms/callback/twilio/incoming", params = params)

    val response = app.response

    //Verify
    response.code mustEqual 200
    response.body.contains(ResourceBundles.Messages.Verify.getString(offerName)) mustEqual true
  }

  "receive user response START (lowercase) from Twilio, with COUNTRY CODE" in {

    val smsService: SmsService = mock[SmsService]
    val app = usingController(new TwilioController(smsService))
    val longCode = UUID.randomUUID.toString
    val offerName = UUID.randomUUID.toString
    val params = Map("From" -> "15102234567", "Body" -> "start", "To" -> longCode) // COUNTRY CODE
    val phoneNumberValidator = new PhoneNumberValidation {}
    val formatPhoneNumber = phoneNumberValidator.getFormattedPhoneNumber(params("From"))
    val subscription = SubscriptionBuilder(phoneNumber = formatPhoneNumber, offerName = offerName).build
    val subscriptionRepository: SubscriptionRepository = mock[SubscriptionRepository]

    //When
    when(smsService.verify(subscription)).thenReturn(SmsServiceResponse("ok"))
    when(smsService.messageType(longCode)).thenReturn(Some("SUBSCRIBE"))
    when(smsService.handleIncoming(subscription, msgBody = params("Body").trim)).thenReturn(IncomingSMSResponse("ok"))
    when(smsService.handleMessage(IncomingSMS(phoneNumber = formatPhoneNumber, longCode = longCode, msgBody = params("Body").trim))).thenReturn(Option(subscription))
    when(subscriptionRepository.findByLongCodePhoneNumber(longCode, params("From"))).thenReturn(Option(subscription))

    //Then
    app.post("/api/sms/callback/twilio/incoming", params = params)

    val response = app.response

    //Verify
    response.code mustEqual 200
    response.body.contains(ResourceBundles.Messages.Verify.getString(offerName)) mustEqual true
  }

  "receive user response START (lowercase) from Twilio, with PLUS COUNTRY CODE (dashes)" in {

    val smsService: SmsService = mock[SmsService]
    val app = usingController(new TwilioController(smsService))
    val longCode = UUID.randomUUID.toString
    val offerName = UUID.randomUUID.toString
    val params = Map("From" -> "+1 510-223-4567", "Body" -> "start", "To" -> longCode) // PLUS COUNTRY CODE (dashes)
    val phoneNumberValidator = new PhoneNumberValidation {}
    val formatPhoneNumber = phoneNumberValidator.getFormattedPhoneNumber(params("From"))
    val subscription = SubscriptionBuilder(phoneNumber = formatPhoneNumber, offerName = offerName).build
    val subscriptionRepository: SubscriptionRepository = mock[SubscriptionRepository]

    //When
    when(smsService.verify(subscription)).thenReturn(SmsServiceResponse("ok"))
    when(smsService.messageType(longCode)).thenReturn(Some("SUBSCRIBE"))
    when(smsService.handleIncoming(subscription, msgBody = params("Body").trim)).thenReturn(IncomingSMSResponse("ok"))
    when(smsService.handleMessage(IncomingSMS(phoneNumber = formatPhoneNumber, longCode = longCode, msgBody = params("Body").trim))).thenReturn(Option(subscription))
    when(subscriptionRepository.findByLongCodePhoneNumber(longCode, params("From"))).thenReturn(Option(subscription))

    //Then
    app.post("/api/sms/callback/twilio/incoming", params = params)

    val response = app.response

    //Verify
    response.code mustEqual 200
    response.body.contains(ResourceBundles.Messages.Verify.getString(offerName)) mustEqual true
  }

  "receive user response START (lowercase) from Twilio, with COUNTRY CODE (dashes)" in {

    val smsService: SmsService = mock[SmsService]
    val app = usingController(new TwilioController(smsService))
    val longCode = UUID.randomUUID.toString
    val offerName = UUID.randomUUID.toString
    val params = Map("From" -> "1-510-223-4567", "Body" -> "start", "To" -> longCode) // COUNTRY CODE (dashes)
    val phoneNumberValidator = new PhoneNumberValidation {}
    val formatPhoneNumber = phoneNumberValidator.getFormattedPhoneNumber(params("From"))
    val subscription = SubscriptionBuilder(phoneNumber = formatPhoneNumber, offerName = offerName).build
    val subscriptionRepository: SubscriptionRepository = mock[SubscriptionRepository]

    //When
    when(smsService.verify(subscription)).thenReturn(SmsServiceResponse("ok"))
    when(smsService.messageType(longCode)).thenReturn(Some("SUBSCRIBE"))
    when(smsService.handleIncoming(subscription, msgBody = params("Body").trim)).thenReturn(IncomingSMSResponse("ok"))
    when(smsService.handleMessage(IncomingSMS(phoneNumber = formatPhoneNumber, longCode = longCode, msgBody = params("Body").trim))).thenReturn(Option(subscription))
    when(subscriptionRepository.findByLongCodePhoneNumber(longCode, params("From"))).thenReturn(Option(subscription))

    //Then
    app.post("/api/sms/callback/twilio/incoming", params = params)

    val response = app.response

    //Verify
    response.code mustEqual 200
    response.body.contains(ResourceBundles.Messages.Verify.getString(offerName)) mustEqual true
  }

  "receive user response START (lowercase) from Twilio, with COUNTRY CODE (dashes, parens)" in {

    val smsService: SmsService = mock[SmsService]
    val app = usingController(new TwilioController(smsService))
    val longCode = UUID.randomUUID.toString
    val offerName = UUID.randomUUID.toString
    val params = Map("From" -> "1 (510) 223-4567", "Body" -> "start", "To" -> longCode) // COUNTRY CODE (dashes, parens)
    val phoneNumberValidator = new PhoneNumberValidation {}
    val formatPhoneNumber = phoneNumberValidator.getFormattedPhoneNumber(params("From"))
    val subscription = SubscriptionBuilder(phoneNumber = formatPhoneNumber, offerName = offerName).build
    val subscriptionRepository: SubscriptionRepository = mock[SubscriptionRepository]

    //When
    when(smsService.verify(subscription)).thenReturn(SmsServiceResponse("ok"))
    when(smsService.messageType(longCode)).thenReturn(Some("SUBSCRIBE"))
    when(smsService.handleIncoming(subscription, msgBody = params("Body").trim)).thenReturn(IncomingSMSResponse("ok"))
    when(smsService.handleMessage(IncomingSMS(phoneNumber = formatPhoneNumber, longCode = longCode, msgBody = params("Body").trim))).thenReturn(Option(subscription))
    when(subscriptionRepository.findByLongCodePhoneNumber(longCode, params("From"))).thenReturn(Option(subscription))

    //Then
    app.post("/api/sms/callback/twilio/incoming", params = params)

    val response = app.response

    //Verify
    response.code mustEqual 200
    response.body.contains(ResourceBundles.Messages.Verify.getString(offerName)) mustEqual true
  }

  "user can not unsubscribe if windows close has ended" in {
    val smsService: SmsService = mock[SmsService]
    val app = usingController(new TwilioController(smsService))
    val longCode = UUID.randomUUID.toString
    val ipoName = "Test Org"
    val params = Map("From" -> "1 (510) 223-4567", "Body" -> "unsubscribe", "To" -> longCode) // COUNTRY CODE (dashes, parens)
    // Set the hour minus 5 so that the window is closed
    val cal = Calendar.getInstance
    cal.add(Calendar.HOUR, -5)
    val phoneNumberValidator = new PhoneNumberValidation {}
    val formatPhoneNumber = phoneNumberValidator.getFormattedPhoneNumber(params("From"))
    val subscription = Subscription(phoneNumber = formatPhoneNumber, priceEndDate = new Timestamp(cal.getTimeInMillis), longCode = longCode, offerName = ipoName)
    val subscriptionRepository: SubscriptionRepository = mock[SubscriptionRepository]

    //When
    when(smsService.messageType(longCode)).thenReturn(Some("unsubscribe"))
    when(smsService.handleIncoming(subscription, msgBody = params("Body").trim)).thenReturn(IncomingSMSResponse("ok"))
    when(smsService.handleMessage(IncomingSMS(phoneNumber = formatPhoneNumber, longCode = longCode, msgBody = params("Body").trim))).thenReturn(Option(subscription))
    when(subscriptionRepository.findByLongCodePhoneNumber(longCode, params("From"))).thenReturn(Option(subscription))

    //Then
    app.post("/api/sms/callback/twilio/incoming", params = params)

    val response = app.response

    //Verify
    response.code mustEqual 200
    response.body.contains(ResourceBundles.Messages.ConfirmAfterWindowClose.getString(ipoName)) mustEqual true
  }

  "user can not subscribe if windows close has ended" in {
    val smsService: SmsService = mock[SmsService]
    val app = usingController(new TwilioController(smsService))
    val longCode = UUID.randomUUID.toString
    val ipoName = "Test Org"
    val params = Map("From" -> "1 (510) 223-4567", "Body" -> "subscribe", "To" -> longCode) // COUNTRY CODE (dashes, parens)
    // Set the hour minus 5 so that the window is closed
    val cal = Calendar.getInstance
    cal.add(Calendar.HOUR, -5)
    val phoneNumberValidator = new PhoneNumberValidation {}
    val formatPhoneNumber = phoneNumberValidator.getFormattedPhoneNumber(params("From"))
    val subscription = Subscription(phoneNumber = formatPhoneNumber, priceEndDate = new Timestamp(cal.getTimeInMillis), longCode = longCode, offerName = ipoName)
    val subscriptionRepository: SubscriptionRepository = mock[SubscriptionRepository]

    //When
    when(smsService.messageType(longCode)).thenReturn(Some("subscribe"))
    when(smsService.handleIncoming(subscription, msgBody = params("Body").trim)).thenReturn(IncomingSMSResponse("ok"))
    when(smsService.handleMessage(IncomingSMS(phoneNumber = formatPhoneNumber, longCode = longCode, msgBody = params("Body").trim))).thenReturn(Option(subscription))
    when(subscriptionRepository.findByLongCodePhoneNumber(longCode, params("From"))).thenReturn(Option(subscription))

    //Then
    app.post("/api/sms/callback/twilio/incoming", params = params)

    val response = app.response

    //Verify
    response.code mustEqual 200
    response.body.contains(ResourceBundles.Messages.ConfirmAfterWindowClose.getString(ipoName)) mustEqual true
  }

  "user can not confirm if windows close has ended" in {
    val smsService: SmsService = mock[SmsService]
    val app = usingController(new TwilioController(smsService))
    val longCode = UUID.randomUUID.toString
    val ipoName = "Test Org"
    val params = Map("From" -> "1 (510) 223-4567", "Body" -> "confirm", "To" -> longCode) // COUNTRY CODE (dashes, parens)
    // Set the hour minus 5 so that the window is closed
    val cal = Calendar.getInstance
    cal.add(Calendar.HOUR, -5)
    val phoneNumberValidator = new PhoneNumberValidation {}
    val formatPhoneNumber = phoneNumberValidator.getFormattedPhoneNumber(params("From"))
    val subscription = Subscription(phoneNumber = formatPhoneNumber, priceEndDate = new Timestamp(cal.getTimeInMillis), longCode = longCode, offerName = ipoName)
    val subscriptionRepository: SubscriptionRepository = mock[SubscriptionRepository]

    //When
    when(smsService.messageType(longCode)).thenReturn(Some("confirm"))
    when(smsService.handleIncoming(subscription, msgBody = params("Body").trim)).thenReturn(IncomingSMSResponse("ok"))
    when(smsService.handleMessage(IncomingSMS(phoneNumber = formatPhoneNumber, longCode = longCode, msgBody = params("Body").trim))).thenReturn(Option(subscription))
    when(subscriptionRepository.findByLongCodePhoneNumber(longCode, params("From"))).thenReturn(Option(subscription))

    //Then
    app.post("/api/sms/callback/twilio/incoming", params = params)

    val response = app.response

    //Verify
    response.code mustEqual 200
    response.body.contains(ResourceBundles.Messages.ConfirmAfterWindowClose.getString(ipoName)) mustEqual true
  }

  "user can confirm since the window has not been closed" in {
    val smsService: SmsService = mock[SmsService]
    val app = usingController(new TwilioController(smsService))
    val longCode = UUID.randomUUID.toString
    val ipoName = "Test Org"
    val params = Map("From" -> "1 (510) 223-4567", "Body" -> "confirm", "To" -> longCode)
    // Set the hour minus 5 so that the window is closed
    val cal = Calendar.getInstance
    cal.add(Calendar.HOUR, 5)
    val phoneNumberValidator = new PhoneNumberValidation {}
    val formatPhoneNumber = phoneNumberValidator.getFormattedPhoneNumber(params("From"))
    val subscription = Subscription(phoneNumber = formatPhoneNumber, priceEndDate = new Timestamp(cal.getTimeInMillis), longCode = longCode, offerName = ipoName)
    val subscriptionRepository: SubscriptionRepository = mock[SubscriptionRepository]

    //When
    when(smsService.handleMessage(IncomingSMS(phoneNumber = formatPhoneNumber, longCode = longCode, msgBody = params("Body").trim))).thenReturn(Option(subscription))
    when(smsService.messageType(longCode)).thenReturn(Some("CONFIRM"))
    when(subscriptionRepository.findByLongCodePhoneNumber(longCode, params("From"))).thenReturn(Option(subscription))

    //Then
    app.post("/api/sms/callback/twilio/incoming", params = params)

    val response = app.response

    //Verify
    response.code mustEqual 200
    response.body.contains(ResourceBundles.Messages.Confirm.getString(ipoName)) mustEqual true
  }

  "user can confirm and receive a response even if the offering name contains some of these characters & < > " in {
    val smsService: SmsService = mock[SmsService]
    val app = usingController(new TwilioController(smsService))
    val longCode = UUID.randomUUID.toString
    val ipoName = "Dave & Busters <><><>"
    val params = Map("From" -> "1 (510) 223-4567", "Body" -> "confirm", "To" -> longCode)
    // Set the hour minus 5 so that the window is closed
    val cal = Calendar.getInstance
    cal.add(Calendar.HOUR, 5)
    val phoneNumberValidator = new PhoneNumberValidation {}
    val formatPhoneNumber = phoneNumberValidator.getFormattedPhoneNumber(params("From"))
    val subscription = Subscription(phoneNumber = formatPhoneNumber, priceEndDate = new Timestamp(cal.getTimeInMillis), longCode = longCode, offerName = ipoName)
    val subscriptionRepository: SubscriptionRepository = mock[SubscriptionRepository]

    //When
    when(smsService.handleMessage(IncomingSMS(phoneNumber = formatPhoneNumber, longCode = longCode, msgBody = params("Body").trim))).thenReturn(Option(subscription))
    when(smsService.messageType(longCode)).thenReturn(Some("CONFIRM"))
    when(subscriptionRepository.findByLongCodePhoneNumber(longCode, params("From"))).thenReturn(Option(subscription))

    //Then
    app.post("/api/sms/callback/twilio/incoming", params = params)

    val response = app.response

    //Verify
    response.code mustEqual 200
    response.body.contains(ResourceBundles.Messages.Confirm.getString(StringEscapeUtils.escapeXml(ipoName))) mustEqual true
  }

  "user can subscribe since the window has not been closed" in {
    val smsService: SmsService = mock[SmsService]
    val app = usingController(new TwilioController(smsService))
    val longCode = UUID.randomUUID.toString
    val offerName = UUID.randomUUID.toString
    val params = Map("From" -> "1 (510) 223-4567", "Body" -> "subscribe", "To" -> longCode)
    // Set the hour minus 5 so that the window is closed
    val cal = Calendar.getInstance
    cal.add(Calendar.HOUR, 5)
    val phoneNumberValidator = new PhoneNumberValidation {}
    val formatPhoneNumber = phoneNumberValidator.getFormattedPhoneNumber(params("From"))
    val subscription = Subscription(phoneNumber = formatPhoneNumber, priceEndDate = new Timestamp(cal.getTimeInMillis), longCode = longCode, offerName = offerName)
    val subscriptionRepository: SubscriptionRepository = mock[SubscriptionRepository]

    //When
    when(smsService.handleMessage(IncomingSMS(phoneNumber = formatPhoneNumber, longCode = longCode, msgBody = params("Body").trim))).thenReturn(Option(subscription))
    when(smsService.messageType(longCode)).thenReturn(Some("SUBSCRIBE"))
    when(subscriptionRepository.findByLongCodePhoneNumber(longCode, params("From"))).thenReturn(Option(subscription))

    //Then
    app.post("/api/sms/callback/twilio/incoming", params = params)

    val response = app.response

    //Verify
    response.code mustEqual 200
    response.body.contains(ResourceBundles.Messages.Verify.getString(offerName)) mustEqual true
  }

  "user can unsubscribe since the window has not been closed" in {
    val smsService: SmsService = mock[SmsService]
    val app = usingController(new TwilioController(smsService))
    val longCode = UUID.randomUUID.toString
    val offerName = UUID.randomUUID.toString
    val params = Map("From" -> "1 (510) 223-4567", "Body" -> "unsubscribe", "To" -> longCode)
    // Set the hour minus 5 so that the window is closed
    val cal = Calendar.getInstance
    cal.add(Calendar.HOUR, 5)
    val phoneNumberValidator = new PhoneNumberValidation {}
    val formatPhoneNumber = phoneNumberValidator.getFormattedPhoneNumber(params("From"))
    val subscription = Subscription(phoneNumber = formatPhoneNumber, priceEndDate = new Timestamp(cal.getTimeInMillis), longCode = longCode, offerName = offerName)
    val subscriptionRepository: SubscriptionRepository = mock[SubscriptionRepository]

    //When
    when(smsService.handleMessage(IncomingSMS(phoneNumber = formatPhoneNumber, longCode = longCode, msgBody = params("Body").trim))).thenReturn(Option(subscription))
    when(smsService.messageType(longCode)).thenReturn(Some("SUBSCRIBE"))
    when(subscriptionRepository.findByLongCodePhoneNumber(longCode, params("From"))).thenReturn(Option(subscription))

    //Then
    app.post("/api/sms/callback/twilio/incoming", params = params)

    val response = app.response

    //Verify
    response.code mustEqual 200
    response.body.contains(ResourceBundles.Messages.Unsubscribe.getString(offerName)) mustEqual true
  }

  "POST /api/sms/callback/twilio/status" should {
    "twilio updates message status" in {
      val smsService: SmsService = mock[SmsService]
      val app = usingController(new TwilioController((smsService)))
      val messageId = UUID.randomUUID.toString
      val messageStatus = "queue"
      val params = Map("MessageId" -> messageId, "messageStatus" -> messageStatus)
      val requestRepository: RequestRepository = mock[RequestRepository]

      // When
      when(smsService.updateMessageStatusByMessageId(messageId, messageStatus)).thenReturn(1)

      // Then
      app.post("/api/sms/callback/twilio/status", params = params)

      val response = app.response

      // Verify
      response.code mustEqual 200
      response.body.contains("done") mustEqual true
    }
  }

}
