package com.loyal3.sms.service.controller

import java.net.URLEncoder
import com.loyal3.sms.config.Config
import com.loyal3.sms.core._
import com.loyal3.sms.core.util.ResourceBundles
import com.loyal3.sms.service.SmsService
import com.loyal3.sms.test.support.scopes.SelfAwareSpecification
import org.mockito.Mockito._
import org.specs2.mock.Mockito

class SubscriptionControllerSpec extends SelfAwareSpecification with FinatraHelpers with Mockito {

  //TechOps test account
  val accountSid: String = "ACbe9ee3a162b1b09d2418a945d38e18fb"
  val authToken: String = "b915fd9e29bd9bfc01b780161e32b333"

  "POST /api/sms/subscriptions" should {
    "create a new subscription" in {
      val subscription = SubscriptionBuilder()
        .withPhoneNumber("14155551212")
        .withTopic("ipo-test")
        .withUserId("abc123")
        .build

      val service = mock[SmsService]
      when(service.subscribe(any[SmsServiceRequest])).thenReturn(SmsServiceResponse("ok", Some(Array()), Some("123")))

      val app = usingController(new SubscriptionController(service))
      app.post("/api/sms/subscriptions", body = subscription)

      // Then
      verify(service).subscribe(any[SmsServiceRequest])
      app.response.code mustEqual 200
    }
    "return 400 if phoneNumber is invalid" in {
      val subscription = SubscriptionBuilder()
        .withPhoneNumber("1415sdl5551212")
        .withTopic("ipo-test")
        .withUserId("abc123")
        .build

      val service = mock[SmsService]
      when(service.subscribe(any[SmsServiceRequest])).
        thenReturn(SmsServiceResponse("fail", Some(Array[String]("Bad number")), None))

      val app = usingController(new SubscriptionController(service))
      app.post("/api/sms/subscriptions", body = subscription)

      // Then
      verify(service).subscribe(any[SmsServiceRequest])
      app.response.code mustEqual 400
    }
    "return 400 if phoneNumber is empty string" in {
      val subscription = SubscriptionBuilder()
        .withPhoneNumber("")
        .withTopic("ipo-test")
        .withUserId("abc123")
        .build

      val service = mock[SmsService]
      when(service.subscribe(any[SmsServiceRequest])).
        thenReturn(SmsServiceResponse("fail", Some(Array[String]("Bad number")), None))

      val app = usingController(new SubscriptionController(service))
      app.post("/api/sms/subscriptions", body = subscription)

      // Then
      verify(service).subscribe(any[SmsServiceRequest])
      app.response.code mustEqual 400
    }

    "return 400 if topic is empty string" in {
      val subscription = SubscriptionBuilder()
        .withPhoneNumber("14155551212")
        .withTopic("")
        .withUserId("abc123")
        .build

      val service = mock[SmsService]
      when(service.subscribe(any[SmsServiceRequest])).
        thenReturn(SmsServiceResponse("fail", Some(Array[String]("Empty topic")), None))

      val app = usingController(new SubscriptionController(service))
      app.post("/api/sms/subscriptions", body = subscription)

      // Then
      verify(service).subscribe(any[SmsServiceRequest])
      app.response.code mustEqual 400
    }

    "return 400 if userId is empty string" in {
      val subscription = SubscriptionBuilder()
        .withPhoneNumber("14155551212")
        .withTopic("ipo-test")
        .withUserId("")
        .build

      val service = mock[SmsService]
      when(service.subscribe(any[SmsServiceRequest])).
        thenReturn(SmsServiceResponse("fail", Some(Array[String]("Empty userId")), None))

      val app = usingController(new SubscriptionController(service))
      app.post("/api/sms/subscriptions", body = subscription)

      // Then
      verify(service).subscribe(any[SmsServiceRequest])
      app.response.code mustEqual 400
    }

    "return 500 if exception arise" in {
      val subscription = SubscriptionBuilder()
        .withPhoneNumber("141sdfs51212")
        .withTopic("")
        .withUserId("")
        .build

      val service = mock[SmsService]

      when(service.subscribe(any[SmsServiceRequest])).thenThrow(new RuntimeException("Foo"))

      val app = usingController(new SubscriptionController(service))
      app.post("/api/sms/subscriptions", body = subscription)

      // Then
      verify(service).subscribe(any[SmsServiceRequest])
      app.response.code mustEqual 500
    }

  }

  "PATCH /api/sms/subscriptions" should {
    "update a subscription" in {
      val subscription = SubscriptionBuilder()
        .withPhoneNumber("14155551212")
        .withTopic("ipo-test")
        .withUserId("abc123")
        .build

      val service = mock[SmsService]
      when(service.resubscribe(any[SmsServiceRequest])).thenReturn(SmsServiceResponse("ok", Some(Array()), Some("123")))

      val app = usingController(new SubscriptionController(service))
      app.patch("/api/sms/subscriptions", body = subscription)

      // Then
      verify(service).resubscribe(any[SmsServiceRequest])
      app.response.code mustEqual 200
    }
  }

  "GET /api/admin/sms/subscriptions/:id" should {
    "return subscription if subscription id exists" in {
      //When
      val subscription = SubscriptionBuilder()
        .withPhoneNumber("14155551212")
        .withTopic("ipo-test")
        .withUserId("abc123")
        .withId("abcde")
        .build

      val service = mock[SmsService]
      when(service.getSubscription(subscription.id)).thenReturn(Some(subscription))

      //Then
      val app = usingController(new SubscriptionController(service))
      app.get("/api/admin/sms/subscriptions/%s".format(subscription.id),
        headers = Map(Config.current.security.headerName -> Config.current.security.authKey))

      //Verify
      verify(service, times(1)).getSubscription(subscription.id)
      app.response.code mustEqual 200
    }

    "return subscription if url encoded subscription id exists" in {
      //When
      val subscription = SubscriptionBuilder()
        .withPhoneNumber("14155551212")
        .withTopic("ipo-test")
        .withUserId("abc123")
        .withId("abcde")
        .build

      val service = mock[SmsService]
      when(service.getSubscription(subscription.id)).thenReturn(Some(subscription))

      //Then
      val app = usingController(new SubscriptionController(service))
      app.get("/api/admin/sms/subscriptions/%s".format(URLEncoder.encode(subscription.id, "UTF-8")),
        headers = Map(Config.current.security.headerName -> Config.current.security.authKey))

      //Verify
      verify(service, times(1)).getSubscription(subscription.id)
      app.response.code mustEqual 200
    }

    "return 404 if subscription id does not exist" in {
      val subscriptionId = "abcde"
      val service = mock[SmsService]
      when(service.getSubscription(subscriptionId)).thenReturn(None)

      val app = usingController(new SubscriptionController(service))
      app.get("/api/admin/sms/subscriptions/%s".format(subscriptionId),
        headers = Map(Config.current.security.headerName -> Config.current.security.authKey))

      // Then
      verify(service, times(1)).getSubscription(subscriptionId)
      app.response.code mustEqual 404
    }

    "return 404 if subscription id is None" in {
      val service = mock[SmsService]
      val app = usingController(new SubscriptionController(service))
      app.get("/api/admin/sms/subscriptions")

      app.response.code mustEqual 404
    }

    "return 400 if missing authToken" in {
      val subscriptionId = "abcde"
      val service = mock[SmsService]

      val app = usingController(new SubscriptionController(service))
      app.get("/api/admin/sms/subscriptions/%s".format(subscriptionId))

      // Then
      app.response.code mustEqual 400
    }

    "return 400 if invalid authToken" in {
      val subscriptionId = "abcde"
      val service = mock[SmsService]

      val app = usingController(new SubscriptionController(service))
      app.get("/api/admin/sms/subscriptions/%s".format(subscriptionId), headers = Map(Config.current.security.headerName -> "wrong-token"))

      // Then
      app.response.code mustEqual 400
    }

    "return 500 if exception arise" in {
      val subscriptionId = "abcde"
      val service = mock[SmsService]

      when(service.getSubscription(subscriptionId)).thenThrow(new RuntimeException("Foo"))

      val app = usingController(new SubscriptionController(service))
      app.get("/api/admin/sms/subscriptions/%s".format(subscriptionId),
        headers = Map(Config.current.security.headerName -> Config.current.security.authKey))

      // Then
      verify(service, times(1)).getSubscription(subscriptionId)
      app.response.code mustEqual 500
    }

  }

  "GET /api/admin/sms/user/:userId/subscriptions" should {
    "return subscriptions if user id exists" in {
      //When
      val userId: String = "abc123"
      val subscriptions = List(
        SubscriptionBuilder()
          .withPhoneNumber("14155551212")
          .withTopic("ipo-test")
          .withUserId(userId)
          .withId("abcde")
          .build,
        SubscriptionBuilder()
          .withPhoneNumber("14155551212")
          .withTopic("ipo-test2")
          .withUserId(userId)
          .withId("abcdef")
          .build
      )

      val service = mock[SmsService]
      when(service.subscriptionsByUser(userId)).thenReturn(subscriptions)

      //Then
      val app = usingController(new SubscriptionController(service))
      app.get("/api/admin/sms/user/%s/subscriptions".format(userId),
        headers = Map(Config.current.security.headerName -> Config.current.security.authKey))

      //Verify
      verify(service, times(1)).subscriptionsByUser(userId)
      app.response.code mustEqual 200
    }

    "return subscriptions if url encoded user id exists" in {
      //When
      val userId: String = "abc123!!"
      val subscriptions = List(
        SubscriptionBuilder()
          .withPhoneNumber("14155551212")
          .withTopic("ipo-test")
          .withUserId(userId)
          .withId("abcde")
          .build,
        SubscriptionBuilder()
          .withPhoneNumber("14155551212")
          .withTopic("ipo-test2")
          .withUserId(userId)
          .withId("abcdef")
          .build
      )

      val service = mock[SmsService]
      when(service.subscriptionsByUser(userId)).thenReturn(subscriptions)

      //Then
      val app = usingController(new SubscriptionController(service))
      app.get("/api/admin/sms/user/%s/subscriptions".format(URLEncoder.encode(userId, "UTF-8")),
        headers = Map(Config.current.security.headerName -> Config.current.security.authKey))

      //Verify
      verify(service, times(1)).subscriptionsByUser(userId)
      app.response.code mustEqual 200
    }

    "return 404 user id does not exist" in {
      val userId = "abcde"
      val service = mock[SmsService]
      when(service.subscriptionsByUser(userId)).thenReturn(Seq())

      val app = usingController(new SubscriptionController(service))
      app.get("/api/admin/sms/user/%s/subscriptions".format(userId),
        headers = Map(Config.current.security.headerName -> Config.current.security.authKey))

      // Then
      verify(service, times(1)).subscriptionsByUser(userId)
      app.response.code mustEqual 404
      app.response.body mustEqual "[ ]"
    }

    "return 400 missing authToken" in {
      val userId = "abcde"
      val service = mock[SmsService]

      val app = usingController(new SubscriptionController(service))
      app.get("/api/admin/sms/user/%s/subscriptions".format(userId))

      // Then
      app.response.code mustEqual 400
    }

    "return 400 invalid authToken" in {
      val userId = "abcde"
      val service = mock[SmsService]

      val app = usingController(new SubscriptionController(service))
      app.get("/api/admin/sms/user/%s/subscriptions".format(userId), headers = Map(Config.current.security.headerName -> "wrong-token"))

      // Then
      app.response.code mustEqual 400
    }

    "return 404 if user id is None" in {
      val service = mock[SmsService]
      val app = usingController(new SubscriptionController(service))
      app.get("/api/admin/sms/user//subscriptions", headers = Map(Config.current.security.headerName -> Config.current.security.authKey))

      app.response.code mustEqual 404
    }

    "return 500 if exception arise" in {
      val subscriptionId = "abcde"
      val service = mock[SmsService]

      when(service.getSubscription(subscriptionId)).thenThrow(new RuntimeException("Foo"))

      val app = usingController(new SubscriptionController(service))
      app.get("/api/admin/sms/subscriptions/%s".format(subscriptionId),
        headers = Map(Config.current.security.headerName -> Config.current.security.authKey))

      // Then
      verify(service, times(1)).getSubscription(subscriptionId)
      app.response.code mustEqual 500
    }
  }

  "GET /api/admin/sms/topic/:topic/responses" should {

    "return responses for a topic" in {
      //When
      val topic: String = "ipo-test"
      val responses = List(
        IncomingSMSBuilder()
          .withUserId("11111")
          .withSubsId("11111")
          .build,
        IncomingSMSBuilder()
          .withUserId("2222")
          .withSubsId("11111")
          .build,
        IncomingSMSBuilder()
          .withPhoneNumber("14155551214")
          .withUserId("3333")
          .withSubsId("11111")
          .build
      )

      val service = mock[SmsService]
      when(service.responsesByTopic(topic, 0L, Long.MaxValue, 0L, Long.MaxValue)).thenReturn(responses)

      //Then
      val app = usingController(new SubscriptionController(service))
      app.get("/api/admin/sms/topic/%s/responses".format(topic))

      //Verify
      verify(service, times(1)).responsesByTopic(topic, 0L, Long.MaxValue, 0L, Long.MaxValue)
      app.response.code mustEqual 200
    }

    "return OK Response for 0 responses" in {
      //When
      val topic: String = "ipo-test"
      val responses = List(
      )

      val service = mock[SmsService]
      when(service.responsesByTopic(topic, 0L, Long.MaxValue, 0L, Long.MaxValue)).thenReturn(responses)

      //Then
      val app = usingController(new SubscriptionController(service))
      app.get("/api/admin/sms/topic/%s/responses".format(topic))

      //Verify
      verify(service, times(1)).responsesByTopic(topic, 0L, Long.MaxValue, 0L, Long.MaxValue)
      app.response.code mustEqual 200
    }

    "return SERVER ERROR for exception" in {
      //When
      val topic: String = "ipo-test"

      val service = mock[SmsService]
      when(service.responsesByTopic(topic, 0L, Long.MaxValue, 0L, Long.MaxValue)).thenThrow(new RuntimeException("Foo"))

      //Then
      val app = usingController(new SubscriptionController(service))
      app.get("/api/admin/sms/topic/%s/responses".format(topic))

      //Verify
      verify(service, times(1)).responsesByTopic(topic, 0L, Long.MaxValue, 0L, Long.MaxValue)
      app.response.code mustEqual 500
    }

    "return responses for a specific ranges start and end sequences" in {
      //When
      val topic: String = "ipo-test-seq"
      val responses = List(
        IncomingSMSBuilder()
          .withUserId("11111")
          .withSubsId("11111")
          .build,
        IncomingSMSBuilder()
          .withUserId("2222")
          .withSubsId("11111")
          .build,
        IncomingSMSBuilder()
          .withPhoneNumber("14155551214")
          .withUserId("3333")
          .withSubsId("11111")
          .build
      )

      val service = mock[SmsService]
      when(service.responsesByTopic(topic, 0L, Long.MaxValue, 0L, Long.MaxValue)).thenReturn(responses)

      //Then
      val app = usingController(new SubscriptionController(service))
      app.get("/api/admin/sms/topic/%s/responses".format(topic))

      //Verify
      verify(service, times(1)).responsesByTopic(topic, 0L, Long.MaxValue, 0L, Long.MaxValue)
      app.response.code mustEqual 200
    }

  }


  "DELETE /api/admin/sms/topic/:topic/phones" should {
    val testTopic = "the_topic_for_test"
    "returns 202 when works ok" in {
      val service = mock[SmsService]
      when(service.releaseProviderPhoneNumbers(testTopic)) thenReturn(true)
      val app = usingController(new SubscriptionController(service))

      app.delete(s"/api/admin/sms/topic/$testTopic/phones")

      verify(service, times(1)).releaseProviderPhoneNumbers(testTopic)
      app.response.code mustEqual 202
    }
    "bad request when delete fails" in {
      val service = mock[SmsService]
      when(service.releaseProviderPhoneNumbers(testTopic)) thenReturn(false)
      val app = usingController(new SubscriptionController(service))

      app.delete(s"/api/admin/sms/topic/$testTopic/phones")

      verify(service, times(1)).releaseProviderPhoneNumbers(testTopic)
      app.response.code mustEqual 400

    }
  }

  "GET /api/admin/sms/topic/:topic/subscriptions" should {
    "return subscriptions if topic exists" in {
      //When
      val userId: String = "abc123"
      val topic: String = "ipo-test"
      val subscriptions = List(
        SubscriptionBuilder()
          .withPhoneNumber("14155551212")
          .withTopic(topic)
          .withUserId(userId)
          .withId("abcde")
          .build,
        SubscriptionBuilder()
          .withPhoneNumber("14155551212")
          .withTopic(topic)
          .withUserId(userId)
          .withId("abcdef")
          .build
      )

      val service = mock[SmsService]
      when(service.subscriptionsByTopic(topic, None)).thenReturn(subscriptions)

      //Then
      val app = usingController(new SubscriptionController(service))
      app.get("/api/admin/sms/topic/%s/subscriptions".format(topic),
        headers = Map(Config.current.security.headerName -> Config.current.security.authKey))

      //Verify
      verify(service, times(1)).subscriptionsByTopic(topic, None)
      app.response.code mustEqual 200
    }

    "return subscriptions if url encoded topic exists" in {
      //When
      val userId: String = "abc123"
      val topic: String = "ipo-test!!"
      val subscriptions = List(
        SubscriptionBuilder()
          .withPhoneNumber("14155551212")
          .withTopic(topic)
          .withUserId(userId)
          .withId("abcde")
          .build,
        SubscriptionBuilder()
          .withPhoneNumber("14155551212")
          .withTopic(topic)
          .withUserId(userId)
          .withId("abcdef")
          .build
      )

      val service = mock[SmsService]
      when(service.subscriptionsByTopic(topic, None)).thenReturn(subscriptions)

      //Then
      val app = usingController(new SubscriptionController(service))
      app.get("/api/admin/sms/topic/%s/subscriptions".format(URLEncoder.encode(topic, "UTF-8")),
        headers = Map(Config.current.security.headerName -> Config.current.security.authKey))

      //Verify
      verify(service, times(1)).subscriptionsByTopic(topic, None)
      app.response.code mustEqual 200
    }

    "return subscriptions for a topic with status query param SUBSCRIBED" in {
      //When
      val userId: String = "abc123"
      val topic: String = "ipo-test"
      val subscriptions = List(
        SubscriptionBuilder()
          .withPhoneNumber("14155551212")
          .withTopic(topic)
          .withState(SubscriptionState.SUBSCRIBED)
          .withUserId(userId)
          .withId("abcde")
          .build,
        SubscriptionBuilder()
          .withPhoneNumber("14155551212")
          .withTopic(topic)
          .withState(SubscriptionState.SUBSCRIBED)
          .withUserId(userId)
          .withId("abcdef")
          .build
      )

      val service = mock[SmsService]
      when(service.subscriptionsByTopic(topic, Some("SUBSCRIBED"))).thenReturn(subscriptions)

      //Then
      val app = usingController(new SubscriptionController(service))
      app.get("/api/admin/sms/topic/%s/subscriptions?status=SUBSCRIBED".format(topic),
        headers = Map(Config.current.security.headerName -> Config.current.security.authKey))

      //Verify
      verify(service, times(1)).subscriptionsByTopic(topic, Some("SUBSCRIBED"))
      app.response.code mustEqual 200
    }

    "return subscriptions for a topic with status query param UNSUBSCRIBED" in {
      //When
      val userId: String = "abc123"
      val topic: String = "ipo-test"
      val subscriptions = List(
        SubscriptionBuilder()
          .withPhoneNumber("14155551212")
          .withTopic(topic)
          .withState(SubscriptionState.UNSUBSCRIBED)
          .withUserId(userId)
          .withId("abcde")
          .build,
        SubscriptionBuilder()
          .withPhoneNumber("14155551212")
          .withTopic(topic)
          .withState(SubscriptionState.UNSUBSCRIBED)
          .withUserId(userId)
          .withId("abcdef")
          .build
      )

      val service = mock[SmsService]
      when(service.subscriptionsByTopic(topic, Some("UNSUBSCRIBED"))).thenReturn(subscriptions)

      //Then
      val app = usingController(new SubscriptionController(service))
      app.get("/api/admin/sms/topic/%s/subscriptions?status=UNSUBSCRIBED".format(topic),
        headers = Map(Config.current.security.headerName -> Config.current.security.authKey))

      //Verify
      verify(service, times(1)).subscriptionsByTopic(topic, Some("UNSUBSCRIBED"))
      app.response.code mustEqual 200
    }

    "return 404 subscriptions for a topic with status query param UNKNOWN" in {
      //When
      val userId: String = "abc123"
      val topic: String = "ipo-test"
      val subscriptions = List(
        // EMPTY RESULTS
      )

      val service = mock[SmsService]
      when(service.subscriptionsByTopic(topic, Some("UNKNOWN"))).thenReturn(subscriptions)

      //Then
      val app = usingController(new SubscriptionController(service))
      app.get("/api/admin/sms/topic/%s/subscriptions?status=UNKNOWN".format(topic),
        headers = Map(Config.current.security.headerName -> Config.current.security.authKey))

      //Verify
      verify(service, times(1)).subscriptionsByTopic(topic, Some("UNKNOWN"))
      app.response.code mustEqual 404
    }


    "return 404 if topic does not exist" in {
      val topic = "abcde"
      val service = mock[SmsService]
      when(service.subscriptionsByTopic(topic, None)).thenReturn(Seq())

      val app = usingController(new SubscriptionController(service))
      app.get("/api/admin/sms/topic/%s/subscriptions".format(topic),
        headers = Map(Config.current.security.headerName -> Config.current.security.authKey))

      // Then
      verify(service, times(1)).subscriptionsByTopic(topic, None)
      app.response.code mustEqual 404
      app.response.body mustEqual "[ ]"
    }

    "return 404 if topic is None" in {
      val service = mock[SmsService]
      val app = usingController(new SubscriptionController(service))
      app.get("/api/admin/sms/topic//subscriptions", headers = Map(Config.current.security.headerName -> Config.current.security.authKey))

      app.response.code mustEqual 404
    }

    "return 400 if missing authToken" in {
      val topic = "abcde"
      val service = mock[SmsService]
      val app = usingController(new SubscriptionController(service))
      app.get("/api/admin/sms/topic/%s/subscriptions".format(topic))

      app.response.code mustEqual 400
    }

    "return 400 if invalid authToken" in {
      val topic = "abcde"
      val service = mock[SmsService]
      val app = usingController(new SubscriptionController(service))
      app.get("/api/admin/sms/topic/%s/subscriptions".format(topic), headers = Map(Config.current.security.headerName -> "wrong-token"))

      app.response.code mustEqual 400
    }

    "return 500 if exception arise" in {
      val topic = "abcde"
      val service = mock[SmsService]

      when(service.subscriptionsByTopic(topic, None)).thenThrow(new RuntimeException("Foo"))

      val app = usingController(new SubscriptionController(service))
      app.get("/api/admin/sms/topic/%s/subscriptions".format(topic),
        headers = Map(Config.current.security.headerName -> Config.current.security.authKey))

      // Then
      verify(service, times(1)).subscriptionsByTopic(topic, None)
      app.response.code mustEqual 500
    }

  }

  "GET /api/admin/sms/phone/:phoneNumber/subscriptions" should {
    "return subscriptions if phone number exists" in {
      //When
      val userId: String = "abc123"
      val topic: String = "ipo-test"
      val phone: String = "14155551212"
      val subscriptions = List(
        SubscriptionBuilder()
          .withPhoneNumber(phone)
          .withTopic(topic)
          .withUserId(userId)
          .withId("abcde")
          .build,
        SubscriptionBuilder()
          .withPhoneNumber(phone)
          .withTopic(topic)
          .withUserId(userId)
          .withId("abcdef")
          .build
      )

      val service = mock[SmsService]
      when(service.subscriptionsByPhoneNumber(phone)).thenReturn(subscriptions)

      //Then
      val app = usingController(new SubscriptionController(service))
      app.get("/api/admin/sms/phone/%s/subscriptions".format(phone),
        headers = Map(Config.current.security.headerName -> Config.current.security.authKey))

      //Verify
      verify(service, times(1)).subscriptionsByPhoneNumber(phone)
      app.response.code mustEqual 200
    }

    "return subscriptions if url encoded phone number exists" in {
      //When
      val userId: String = "abc123"
      val topic: String = "ipo-test"
      val phone: String = "1(415)5551212"
      val cleanPhone: String = phone.replaceAll("[^0-9]*", "")
      val urlEncodedPhone: String = URLEncoder.encode(phone, "UTF-8")
      val subscriptions = List(
        SubscriptionBuilder()
          .withPhoneNumber(cleanPhone)
          .withTopic(topic)
          .withUserId(userId)
          .withId("abcde")
          .build,
        SubscriptionBuilder()
          .withPhoneNumber(cleanPhone)
          .withTopic(topic)
          .withUserId(userId)
          .withId("abcdef")
          .build
      )

      val service = mock[SmsService]
      when(service.subscriptionsByPhoneNumber(phone)).thenReturn(subscriptions)

      //Then
      val app = usingController(new SubscriptionController(service))

      app.get("/api/admin/sms/phone/%s/subscriptions".format(urlEncodedPhone),
        headers = Map(Config.current.security.headerName -> Config.current.security.authKey))

      //Verify
      verify(service, times(1)).subscriptionsByPhoneNumber(phone)
      app.response.code mustEqual 200
    }

    "return 404 if phone number does not exist" in {
      val phone: String = "14155551212"
      val service = mock[SmsService]
      when(service.subscriptionsByPhoneNumber(phone)).thenReturn(Seq())

      val app = usingController(new SubscriptionController(service))
      app.get("/api/admin/sms/phone/%s/subscriptions".format(phone),
        headers = Map(Config.current.security.headerName -> Config.current.security.authKey))

      // Then
      verify(service, times(1)).subscriptionsByPhoneNumber(phone)
      app.response.code mustEqual 404
      app.response.body mustEqual "[ ]"
    }

    "return 400 if missing authToken" in {
      val phone: String = "14155551212"
      val service = mock[SmsService]

      val app = usingController(new SubscriptionController(service))
      app.get("/api/admin/sms/phone/%s/subscriptions".format(phone))

      // Then
      app.response.code mustEqual 400
    }

    "return 400 if invalid authtoken" in {
      val phone: String = "14155551212"
      val service = mock[SmsService]

      val app = usingController(new SubscriptionController(service))
      app.get("/api/admin/sms/phone/%s/subscriptions".format(phone), headers = Map(Config.current.security.headerName -> "wrong-token"))

      // Then
      app.response.code mustEqual 400
    }

    "return 404 if phone number is None" in {
      val service = mock[SmsService]
      val app = usingController(new SubscriptionController(service))
      app.get("/api/admin/sms/phone//subscriptions", headers = Map(Config.current.security.headerName -> Config.current.security.authKey))

      app.response.code mustEqual 404
    }

    "return 500 if exception arise" in {
      val phone: String = "14155551212"
      val service = mock[SmsService]

      when(service.subscriptionsByPhoneNumber(phone)).thenThrow(new RuntimeException("Foo"))

      val app = usingController(new SubscriptionController(service))
      app.get("/api/admin/sms/phone/%s/subscriptions".format(phone), headers = Map(Config.current.security.headerName -> Config.current.security.authKey))

      // Then
      verify(service, times(1)).subscriptionsByPhoneNumber(phone)
      app.response.code mustEqual 500
    }

  }

  "POST /api/admin/sms/broadcast" should {
    "Send new messages" in {
      // Create subscriptions so that they can receive the broadcast
      val subscription1 = SubscriptionBuilder()
        .withPhoneNumber("14155551212")
        .withTopic("ipo-test")
        .withUserId("abc123")
        .build
      val subscription2 = SubscriptionBuilder()
        .withPhoneNumber("14155551211")
        .withTopic("ipo-test")
        .withUserId("abc435")
        .build

      val broadcast = BroadcastBuilder()
        .withMsgBody("This a test message body")
        .withTopic("ipo-test")
        .withUserIds(List("abc123", "abc435"))
        .build

      val service = mock[SmsService]
      when(service.send(any[Broadcast])).thenReturn(SmsServiceResponse("ok"))

      val app = usingController(new SubscriptionController(service))
      app.post("/api/admin/sms/broadcast", body = broadcast)

      // Then
      verify(service).send(any[Broadcast])
      app.response.code mustEqual 200
    }
    "return 400 if topic or msgbody was not supplied" in {
      val subscription = SubscriptionBuilder()
        .withPhoneNumber("1415sdl5551212")
        .withTopic("ipo-test")
        .withUserId("abc123")
        .build
      val broadcast = BroadcastBuilder()
        .withMsgBody("This a test message body")
        .withTopic("ipo-test")
        .withUserIds(List("abc123"))
        .build

      val service = mock[SmsService]
      when(service.send(any[Broadcast])).
        thenReturn(SmsServiceResponse("fail", Some(Array[String](ResourceBundles.Messages.ErrorTopicMsgBody.getString)), None))

      val app = usingController(new SubscriptionController(service))
      app.post("/api/admin/sms/broadcast", body = broadcast)

      // Then
      verify(service).send(any[Broadcast])
      app.response.code mustEqual 400
    }
    "return 200 if user does not exist in database" in {
      val broadcast = BroadcastBuilder()
        .withMsgBody("This a test message body")
        .withTopic("ipo-test")
        .withUserIds(List("someUser"))
        .build

      val service = mock[SmsService]
      when(service.send(any[Broadcast])).
        thenReturn(SmsServiceResponse("ok", None, None))

      val app = usingController(new SubscriptionController(service))
      app.post("/api/admin/sms/broadcast", body = broadcast)

      // Then
      verify(service).send(any[Broadcast])
      app.response.code mustEqual 200
    }

    "return 200 and only one message sent succesfully if one user exist and other doesn't" in {
      val subscription = SubscriptionBuilder()
        .withPhoneNumber("14155551212")
        .withTopic("ipo-test")
        .withUserId("testUser1")
        .build
      val broadcast = BroadcastBuilder()
        .withMsgBody("This a test message body")
        .withTopic("ipo-test")
        .withUserIds(List("someUser", "testUser1", "anotherUser"))
        .build

      val service = mock[SmsService]
      when(service.send(any[Broadcast])).
        thenReturn(SmsServiceResponse("ok", Some(Array[String](ResourceBundles.Messages.MessagesSent.getString format 1,
        ResourceBundles.Messages.MessagesNotSent.getString format 2)), None))

      val app = usingController(new SubscriptionController(service))
      app.post("/api/admin/sms/broadcast", body = broadcast)

      // Then
      verify(service).send(any[Broadcast])
      app.response.code mustEqual 200
    }

    "return 500 if exception arise" in {
      val broadcast = BroadcastBuilder()
        .withTopic("ipo-test")
        .withUserIds(List("someUser", "testUser1", "anotherUser"))
        .build

      val service = mock[SmsService]

      when(service.send(any[Broadcast])).thenThrow(new RuntimeException("Foo"))

      val app = usingController(new SubscriptionController(service))
      app.post("/api/admin/sms/broadcast", body = broadcast)

      // Then
      verify(service).send(any[Broadcast])
      app.response.code mustEqual 500
    }

  }
}
