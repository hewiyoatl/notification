package com.loyal3.sms.service

import com.loyal3.sms.test.support.scopes.SelfAwareSpecification
import java.util.UUID
import com.loyal3.sms.core._
import com.loyal3.sms.service.repository._
import org.scalatest.mock.MockitoSugar
import com.loyal3.sms.service.provider.{SendRequest, SmsProvider}
import org.mockito.Mockito._
import com.loyal3.sms.core.validators.PhoneNumberValidation
import com.loyal3.sms.core.SubscriptionBuilder
import com.loyal3.sms.core.Subscription
import com.loyal3.sms.core.IncomingSMS
import com.loyal3.sms.core.SmsServiceResponse

class SmsServiceSpec extends SelfAwareSpecification with MockitoSugar with PhoneNumberValidation {

  "#subscribe" should {
    "get an exception if failed to get a valid phone number to send out sms" in {
      val userId: String = UUID.randomUUID().toString
      val blahPhoneNumber: String = "8005551212"
      val blahTopic: String = "some_topic"
      val subId = "1234567890"
      val subscription: Subscription = SubscriptionBuilder()
        .withPhoneNumber(blahPhoneNumber)
        .withTopic(blahTopic)
        .withUserId(userId)
        .withState(SubscriptionState.SUBSCRIBED)
        .withLongCode("")
        .withId(subId)
        .build

      val mockRepoSubscription = mock[SubscriptionRepository]
      val mockRepoResponse = mock[ResponseRepository]

      val mockProvider = mock[SmsProvider]
      val mockRepoRequest = mock[RequestRepository]
      val mockRepoTopicStats = mock[TopicStatsRepository]
      val mockRepoWindowClose = mock[WindowCloseRepository]
      val service = new SmsServiceImpl(mockRepoSubscription, mockRepoResponse, mockRepoRequest, mockRepoTopicStats, mockRepoWindowClose, mockProvider)


      when(mockRepoSubscription.create(Subscription(phoneNumber = blahPhoneNumber, topic = blahTopic, userId = userId, longCode = ""))).thenReturn(subId)
      when(mockRepoSubscription.isDuplicatedSubscription(Subscription(topic = blahTopic, userId = userId, phoneNumber = blahPhoneNumber))).thenReturn(false)
      // When did not get a phone number
      when(mockProvider.getPhoneNumber(Subscription(topic = blahTopic))).thenReturn(None)


      var hasException = false;
      try{
        service.subscribe(subscription)
      }
      catch{
        case e: Exception=>
          hasException = true
          e.getMessage mustEqual s"SmsServiceImpl.subscribe did not send out a message because we failed to get a valid phone number, subscriptionId=${subId}"
      }

      // Then
      hasException mustEqual true
    }

    "return OK response for a valid request" in {
      val userId: String = UUID.randomUUID().toString
      val blahPhoneNumber: String = "8005551212"
      val longCode: String = UUID.randomUUID.toString
      val blahTopic: String = "some_topic"
      val subscription: Subscription = SubscriptionBuilder()
        .withPhoneNumber(blahPhoneNumber)
        .withTopic(blahTopic)
        .withUserId(userId)
        .withState(SubscriptionState.SUBSCRIBED)
        .withLongCode(longCode)
        .withId("1234567890")
        .build

      val mockRepoSubscription = mock[SubscriptionRepository]
      val mockRepoResponse = mock[ResponseRepository]

      val mockProvider = mock[SmsProvider]
      val mockRepoRequest = mock[RequestRepository]
      val mockRepoTopicStats = mock[TopicStatsRepository]
      val mockRepoWindowClose = mock[WindowCloseRepository]
      val service = new SmsServiceImpl(mockRepoSubscription, mockRepoResponse, mockRepoRequest, mockRepoTopicStats, mockRepoWindowClose, mockProvider)
      val id = "1234567890"

      // When
      when(mockRepoSubscription.create(Subscription(phoneNumber = blahPhoneNumber, topic = blahTopic, userId = userId, longCode = longCode))).thenReturn(id)
      when(mockRepoSubscription.isDuplicatedSubscription(Subscription(topic = blahTopic, userId = userId, phoneNumber = blahPhoneNumber))).thenReturn(false)
      when(mockProvider.getPhoneNumber(Subscription(topic = blahTopic))).thenReturn(Some(longCode))


      val response: SmsServiceResponse = service.subscribe(subscription)

      // Then
      verify(mockProvider, times(1)).send(_, _)

      response.code mustEqual "ok"
      response.subscriptionId mustNotEqual null
      response.subscriptionId equals Some(id)
    }

    "return FAILED if phone number is 9 digits" in {
      val phoneNumber: String = "800555121" // 9 digits long

      val blahUserId: String = UUID.randomUUID().toString
      val blahTopic: String = "some_topic"
      val request = SubscriptionBuilder()
        .withPhoneNumber(phoneNumber)
        .withTopic(blahTopic)
        .withUserId(blahUserId)
        .build

      val mockRepoSubscription = mock[SubscriptionRepository]
      val mockRepoResponse = mock[ResponseRepository]

      val mockProvider = mock[SmsProvider]
      val mockRepoRequest = mock[RequestRepository]
      val mockRepoTopicStats = mock[TopicStatsRepository]
      val mockRepoWindowClose = mock[WindowCloseRepository]
      val service = new SmsServiceImpl(mockRepoSubscription, mockRepoResponse, mockRepoRequest, mockRepoTopicStats, mockRepoWindowClose, mockProvider)

      // When
      val response: SmsServiceResponse = service.subscribe(request)

      // Then
      verify(mockProvider, times(0)).send(_, _)
      response.code mustEqual "failed"
      response.subscriptionId mustNotEqual null
    }

    "return OK even if subscribing multiple times but we store only one entry" in {
      val userId: String = UUID.randomUUID().toString
      val blahPhoneNumber: String = "8005551212"
      val longCode: String = UUID.randomUUID.toString
      val blahTopic: String = UUID.randomUUID().toString
      val id = "1234567890"
      val subscription: Subscription = SubscriptionBuilder()
        .withPhoneNumber(blahPhoneNumber)
        .withTopic(blahTopic)
        .withUserId(userId)
        .withState(SubscriptionState.SUBSCRIBED)
        .withLongCode(longCode)
        .withId(id)
        .build
      val subscription2: Subscription = SubscriptionBuilder()
        .withPhoneNumber(blahPhoneNumber)
        .withTopic(blahTopic)
        .withUserId(userId)
        .withState(SubscriptionState.SUBSCRIBED)
        .withLongCode(longCode)
        .withId(id)
        .build

      val mockRepoSubscription = mock[SubscriptionRepository]
      val mockRepoResponse = mock[ResponseRepository]

      val mockProvider = mock[SmsProvider]
      val mockRepoRequest = mock[RequestRepository]
      val mockRepoTopicStats = mock[TopicStatsRepository]
      val mockRepoWindowClose = mock[WindowCloseRepository]
      val service = new SmsServiceImpl(mockRepoSubscription, mockRepoResponse, mockRepoRequest, mockRepoTopicStats, mockRepoWindowClose, mockProvider)

      // When
      when(mockRepoSubscription.create(Subscription(phoneNumber = blahPhoneNumber, topic = blahTopic, userId = userId, longCode = longCode))).thenReturn(id)
      when(mockRepoSubscription.isDuplicatedSubscription(Subscription(topic = blahTopic, userId = userId, phoneNumber = blahPhoneNumber))).thenReturn(false)
      when(mockRepoSubscription.isDuplicatedSubscription(Subscription(topic = blahTopic, userId = userId, phoneNumber = blahPhoneNumber))).thenReturn(true)

      val response1: SmsServiceResponse = service.subscribe(subscription)
      val response2: SmsServiceResponse = service.subscribe(subscription)

      // Then
      response2.code mustEqual "duplicate"
      response2.subscriptionId mustEqual None
    }

  }

  "return FAILED if phone number is NULL" in {
    val blahUserId: String = UUID.randomUUID().toString
    val blahTopic: String = "some_topic"
    val request = SubscriptionBuilder()
      .withPhoneNumber(null)
      .withTopic(blahTopic)
      .withUserId(blahUserId)
      .build

    val mockRepoSubscription = mock[SubscriptionRepository]
    val mockRepoResponse = mock[ResponseRepository]

    val mockProvider = mock[SmsProvider]
    val mockRepoRequest = mock[RequestRepository]
    val mockRepoTopicStats = mock[TopicStatsRepository]
    val mockRepoWindowClose = mock[WindowCloseRepository]
    val service = new SmsServiceImpl(mockRepoSubscription, mockRepoResponse, mockRepoRequest, mockRepoTopicStats, mockRepoWindowClose, mockProvider)

    // When
    val response: SmsServiceResponse = service.subscribe(request)

    // Then
    verify(mockProvider, times(0)).send(_, _)
    response.code mustEqual "failed"
    response.subscriptionId mustNotEqual null
  }

  "return FAILED if userId is NULL" in {
    val blahPhone: String = "4155551212"
    val blahTopic: String = "some_topic"
    val request = SubscriptionBuilder()
      .withPhoneNumber(blahPhone)
      .withTopic(blahTopic)
      .withUserId(null)
      .build
    val mockRepoSubscription = mock[SubscriptionRepository]
    val mockRepoResponse = mock[ResponseRepository]

    val mockProvider = mock[SmsProvider]
    val mockRepoRequest = mock[RequestRepository]
    val mockRepoTopicStats = mock[TopicStatsRepository]
    val mockRepoWindowClose = mock[WindowCloseRepository]
    val service = new SmsServiceImpl(mockRepoSubscription, mockRepoResponse, mockRepoRequest, mockRepoTopicStats, mockRepoWindowClose, mockProvider)

    // When
    val response: SmsServiceResponse = service.subscribe(request)

    // Then
    verify(mockProvider, times(0)).send(_, _)
    response.code mustEqual "failed"
    response.subscriptionId mustNotEqual null
  }

  "return FAILED if topic is NULL" in {
    val blahPhone: String = "4155551212"
    val blahUserId: String = UUID.randomUUID().toString

    val request = SubscriptionBuilder()
      .withPhoneNumber(blahPhone)
      .withTopic(null)
      .withUserId(blahUserId)
      .build
    val mockRepoSubscription = mock[SubscriptionRepository]
    val mockRepoResponse = mock[ResponseRepository]
    val mockProvider = mock[SmsProvider]
    val mockRepoRequest = mock[RequestRepository]
    val mockRepoTopicStats = mock[TopicStatsRepository]
    val mockRepoWindowClose = mock[WindowCloseRepository]
    val service = new SmsServiceImpl(mockRepoSubscription, mockRepoResponse, mockRepoRequest, mockRepoTopicStats, mockRepoWindowClose, mockProvider)

    // When
    val response: SmsServiceResponse = service.subscribe(request)

    // Then
    verify(mockProvider, times(0)).send(_, _)
    response.code mustEqual "failed"
    response.subscriptionId mustNotEqual null
  }

  "#resubscribe" should {
    "return OK response for a valid resubscribe request" in {
      val id = "1234567890"
      val userId: String = UUID.randomUUID().toString
      val blahPhoneNumber: String = "8005551212"
      val longCode: String = UUID.randomUUID.toString
      val blahTopic: String = "some_topic"
      val subscription: Subscription = SubscriptionBuilder()
        .withPhoneNumber(blahPhoneNumber)
        .withTopic(blahTopic)
        .withUserId(userId)
        .withState(SubscriptionState.SUBSCRIBED)
        .withLongCode(longCode)
        .withId(id)
        .build

      val mockRepoSubscription = mock[SubscriptionRepository]
      val mockRepoResponse = mock[ResponseRepository]

      val mockProvider = mock[SmsProvider]
      val mockRepoRequest = mock[RequestRepository]
      val mockRepoTopicStats = mock[TopicStatsRepository]
      val mockRepoWindowClose = mock[WindowCloseRepository]
      val service = new SmsServiceImpl(mockRepoSubscription, mockRepoResponse, mockRepoRequest, mockRepoTopicStats, mockRepoWindowClose, mockProvider)

      // When
      when(mockRepoSubscription.findByUserIdTopicAndPhoneNumber(userId, blahTopic, blahPhoneNumber))
        .thenReturn(Some(Subscription(id = id, phoneNumber = blahPhoneNumber, topic = blahTopic, userId = userId, longCode = longCode, state = SubscriptionState.UNSUBSCRIBED)))
      when(mockProvider.getPhoneNumber(Subscription(topic = blahTopic))).thenReturn(Some(longCode))

      val response: SmsServiceResponse = service.resubscribe(subscription)

      // Then
      verify(mockRepoSubscription, times(1)).updateStateLongCodeBySubsId(id, SubscriptionState.SUBSCRIBED, longCode)
      verify(mockRepoRequest, times(1)).create(_)
      verify(mockProvider, times(1)).send(_, _)

      response.code mustEqual "ok"
      response.subscriptionId mustNotEqual null
      response.subscriptionId equals Some(id)
    }
  }

  "#unsubscribe" should {
    "return OK response for a valid unsubscribe request" in {
      val phoneNumber: String = "8005551210"
      val blahUserId: String = UUID.randomUUID().toString
      val longCode: String = UUID.randomUUID.toString
      val blahTopic: String = "some_topic"
      val request = SubscriptionBuilder()
        .withPhoneNumber(phoneNumber)
        .withTopic(blahTopic)
        .withUserId(blahUserId)
        .withLongCode(longCode)
        .build

      val phoneNumberValidator = new PhoneNumberValidation {}
      val formatPhoneNumber = phoneNumberValidator.getFormattedPhoneNumber(phoneNumber)

      val mockRepoSubscription = mock[SubscriptionRepository]
      val mockRepoResponse = mock[ResponseRepository]

      val mockProvider = mock[SmsProvider]
      val mockRepoRequest = mock[RequestRepository]
      val mockRepoTopicStats = mock[TopicStatsRepository]
      val mockRepoWindowClose = mock[WindowCloseRepository]

      val service = new SmsServiceImpl(mockRepoSubscription, mockRepoResponse, mockRepoRequest, mockRepoTopicStats, mockRepoWindowClose, mockProvider)

      // When
      when(mockProvider.getPhoneNumber(Subscription(topic = blahTopic))).thenReturn(Some(longCode))
      val response: SmsServiceResponse = service.unsubscribe(request)

      // Then
      verify(mockRepoSubscription, times(1)).updateStateByLongCode(longCode, formatPhoneNumber, SubscriptionState.UNSUBSCRIBED)
      response.code mustEqual "ok"
      response.subscriptionId mustNotEqual null
    }
  }

  "#subscriptionsByUser" should {
    "return an empty List of subscriptions for an unknown user id" in {
      val userId: String = UUID.randomUUID().toString
      val mockRepoSubscription = mock[SubscriptionRepository]
      val mockRepoResponse = mock[ResponseRepository]

      val mockProvider = mock[SmsProvider]
      val mockRepoRequest = mock[RequestRepository]
      val mockRepoTopicStats = mock[TopicStatsRepository]
      val mockRepoWindowClose = mock[WindowCloseRepository]
      val service = new SmsServiceImpl(mockRepoSubscription, mockRepoResponse, mockRepoRequest, mockRepoTopicStats, mockRepoWindowClose, mockProvider)

      // When
      when(mockRepoSubscription.findByUserId(userId)).thenReturn(Seq())
      val response = service.subscriptionsByUser(userId)

      // Then
      verify(mockRepoSubscription, times(1)).findByUserId(userId)
      response.size mustEqual 0
    }

    "return a List of subscriptions for a known user id" in {
      val userId: String = UUID.randomUUID().toString
      val mockRepoSubscription = mock[SubscriptionRepository]
      val mockRepoResponse = mock[ResponseRepository]
      val mockProvider = mock[SmsProvider]
      val mockRepoRequest = mock[RequestRepository]
      val mockRepoTopicStats = mock[TopicStatsRepository]
      val mockRepoWindowClose = mock[WindowCloseRepository]
      val service = new SmsServiceImpl(mockRepoSubscription, mockRepoResponse, mockRepoRequest, mockRepoTopicStats, mockRepoWindowClose, mockProvider)

      // When
      when(mockRepoSubscription.findByUserId(userId)).thenReturn(Seq(new Subscription()))
      val response = service.subscriptionsByUser(userId)

      // Then
      verify(mockRepoSubscription, times(1)).findByUserId(userId)
      response.size mustEqual 1
    }
  }

  "#subscriptionsByTopic" should {
    "return an empty List of subscriptions for an unknown topic" in {
      val topic = "some_topic"
      val mockRepoSubscription = mock[SubscriptionRepository]
      val mockRepoResponse = mock[ResponseRepository]
      val mockProvider = mock[SmsProvider]
      val mockRepoRequest = mock[RequestRepository]
      val mockRepoTopicStats = mock[TopicStatsRepository]
      val mockRepoWindowClose = mock[WindowCloseRepository]
      val service = new SmsServiceImpl(mockRepoSubscription, mockRepoResponse, mockRepoRequest, mockRepoTopicStats, mockRepoWindowClose, mockProvider)

      // When
      when(mockRepoSubscription.findByTopic(topic, None)).thenReturn(Seq())
      val response = service.subscriptionsByTopic(topic)

      // Then
      verify(mockRepoSubscription, times(1)).findByTopic(topic, None)
      response.size mustEqual 0
    }

    "return a List of subscriptions for a known topic" in {
      val topic = "some_topic"
      val mockRepoSubscription = mock[SubscriptionRepository]
      val mockRepoResponse = mock[ResponseRepository]
      val mockProvider = mock[SmsProvider]
      val mockRepoRequest = mock[RequestRepository]
      val mockRepoTopicStats = mock[TopicStatsRepository]
      val mockRepoWindowClose = mock[WindowCloseRepository]
      val service = new SmsServiceImpl(mockRepoSubscription, mockRepoResponse, mockRepoRequest, mockRepoTopicStats, mockRepoWindowClose, mockProvider)

      // When
      when(mockRepoSubscription.findByTopic(topic, None)).thenReturn(Seq(new Subscription()))
      val response = service.subscriptionsByTopic(topic)

      // Then
      verify(mockRepoSubscription, times(1)).findByTopic(topic, None)
      response.size mustEqual 1
    }

    "return a List of subscriptions for a known topic, filtered by status" in {
      val topic = "some_topic"
      val mockRepoSubscription = mock[SubscriptionRepository]
      val mockRepoResponse = mock[ResponseRepository]
      val mockProvider = mock[SmsProvider]
      val mockRepoRequest = mock[RequestRepository]
      val mockRepoTopicStats = mock[TopicStatsRepository]
      val mockRepoWindowClose = mock[WindowCloseRepository]
      val service = new SmsServiceImpl(mockRepoSubscription, mockRepoResponse, mockRepoRequest, mockRepoTopicStats, mockRepoWindowClose, mockProvider)

      // When
      when(mockRepoSubscription.findByTopic(topic, Some("SUBSCRIBED"))).thenReturn(Seq(new Subscription()))
      val response = service.subscriptionsByTopic(topic, Some("SUBSCRIBED"))

      // Then
      verify(mockRepoSubscription, times(1)).findByTopic(topic, Some("SUBSCRIBED"))
      response.size mustEqual 1
    }

  }

  "#responsesByTopic" should {
    "return an empty List of responses for an unknown topic" in {
      val topic = "some_topic"
      val mockRepoSubscription = mock[SubscriptionRepository]
      val mockRepoResponse = mock[ResponseRepository]
      val mockProvider = mock[SmsProvider]
      val mockRepoRequest = mock[RequestRepository]
      val mockRepoTopicStats = mock[TopicStatsRepository]
      val mockRepoWindowClose = mock[WindowCloseRepository]
      val service = new SmsServiceImpl(mockRepoSubscription, mockRepoResponse, mockRepoRequest, mockRepoTopicStats, mockRepoWindowClose, mockProvider)

      // When
      when(mockRepoResponse.findByTopic(topic, 0L, Long.MaxValue, 0L, Long.MaxValue)).thenReturn(Seq())
      val response = service.responsesByTopic(topic, 0L, Long.MaxValue, 0L, Long.MaxValue)

      // Then
      verify(mockRepoResponse, times(1)).findByTopic(topic, 0L, Long.MaxValue, 0L, Long.MaxValue)
      response.size mustEqual 0
    }

    "return a List of subscriptions for a known topic" in {
      val topic = "some_topic"
      val mockRepoSubscription = mock[SubscriptionRepository]
      val mockRepoResponse = mock[ResponseRepository]
      val mockProvider = mock[SmsProvider]
      val mockRepoRequest = mock[RequestRepository]
      val mockRepoTopicStats = mock[TopicStatsRepository]
      val mockRepoWindowClose = mock[WindowCloseRepository]
      val service = new SmsServiceImpl(mockRepoSubscription, mockRepoResponse, mockRepoRequest, mockRepoTopicStats, mockRepoWindowClose, mockProvider)

      // When
      when(mockRepoResponse.findByTopic(topic, 0L, Long.MaxValue, 0L, Long.MaxValue)).thenReturn(Seq(new IncomingSMS()))
      val response = service.responsesByTopic(topic, 0L, Long.MaxValue, 0L, Long.MaxValue)

      // Then
      verify(mockRepoResponse, times(1)).findByTopic(topic, 0L, Long.MaxValue, 0L, Long.MaxValue)
      response.size mustEqual 1
    }

  }

  "#releaseProviderPhoneNumbers" should {
    val testTopic = "the_test_topic"
    "release the phone for the provider" in {
      val theLongCode = "+15555555555"
      val subscriberPhone = "+14154154115"
      val mockRepoSubscription = mock[SubscriptionRepository]
      val mockProvider = mock[SmsProvider]
      val subscription = mock[Subscription]
      when(subscription.state).thenReturn(SubscriptionState.SUBSCRIBED)
      when(subscription.longCode).thenReturn(theLongCode)
      when(subscription.phoneNumber).thenReturn(subscriberPhone)

      when(mockProvider.releasePhoneNumber(theLongCode)).thenReturn(true)

      when(mockRepoSubscription.findByTopic(testTopic))
        .thenReturn(Seq(subscription))
        .thenReturn(Nil)
      when(mockRepoSubscription.updateStateByLongCode(theLongCode,subscriberPhone,SubscriptionState.TERMINATED)).thenReturn(1)
      val service = new SmsServiceImpl(mockRepoSubscription, provider = mockProvider)

      val response = service.releaseProviderPhoneNumbers(testTopic)

      verify(mockRepoSubscription,times(1)).findByTopic(testTopic, null)
      verify(mockProvider,times(1)).releasePhoneNumber(theLongCode)
      verify(mockRepoSubscription, times(1)).updateStateByLongCode(theLongCode,subscriberPhone,SubscriptionState.TERMINATED)
      response mustEqual(true)
    }
    "don't change the status of unsubscribed subscriptions" in {
      val theLongCode = "+15555555555"
      val subscriberPhone = "+14154154115"
      val mockRepoSubscription = mock[SubscriptionRepository]
      val mockProvider = mock[SmsProvider]
      val subscription = mock[Subscription]
      when(subscription.state).thenReturn(SubscriptionState.UNSUBSCRIBED)
      when(subscription.longCode).thenReturn(theLongCode)
      when(subscription.phoneNumber).thenReturn(subscriberPhone)

      when(mockProvider.releasePhoneNumber(theLongCode)).thenReturn(true)

      when(mockRepoSubscription.findByTopic(testTopic))
        .thenReturn(Seq(subscription))
        .thenReturn(Nil)
      when(mockRepoSubscription.updateStateByLongCode(theLongCode,subscriberPhone,SubscriptionState.TERMINATED)).thenReturn(1)
      val service = new SmsServiceImpl(mockRepoSubscription, provider = mockProvider)

      val response = service.releaseProviderPhoneNumbers(testTopic)

      verify(mockRepoSubscription,times(1)).findByTopic(testTopic, null)
      verify(mockProvider,times(1)).releasePhoneNumber(theLongCode)
      verify(mockRepoSubscription, times(0)).updateStateByLongCode(theLongCode,subscriberPhone,SubscriptionState.TERMINATED)
      response mustEqual(true)
    }
    "not change subscription status if phone not released" in {
      val theLongCode = "+15555555555"
      val subscriberPhone = "+14154154115"
      val mockRepoSubscription = mock[SubscriptionRepository]
      val mockProvider = mock[SmsProvider]
      val subscription = mock[Subscription]
      when(subscription.state).thenReturn(SubscriptionState.SUBSCRIBED)
      when(subscription.longCode).thenReturn(theLongCode)
      when(subscription.phoneNumber).thenReturn(subscriberPhone)

      when(mockProvider.releasePhoneNumber(theLongCode)).thenReturn(false)

      when(mockRepoSubscription.findByTopic(testTopic))
        .thenReturn(Seq(subscription))
      val service = new SmsServiceImpl(mockRepoSubscription, provider = mockProvider)

      val response = service.releaseProviderPhoneNumbers(testTopic)

      verify(mockRepoSubscription,times(1)).findByTopic(testTopic, null)
      verify(mockProvider,times(1)).releasePhoneNumber(theLongCode)
      verify(mockRepoSubscription, times(0)).updateStateByLongCode(theLongCode,subscriberPhone,SubscriptionState.TERMINATED)
      response mustEqual(false)
    }
  }

  "#getSubscription" should {
    "return an empty List of subscriptions for an unknown subscription id" in {
      val subscriptionId = UUID.randomUUID().toString
      val mockRepoSubscription = mock[SubscriptionRepository]
      val mockRepoResponse = mock[ResponseRepository]
      val mockProvider = mock[SmsProvider]
      val mockRepoRequest = mock[RequestRepository]
      val mockRepoTopicStats = mock[TopicStatsRepository]
      val mockRepoWindowClose = mock[WindowCloseRepository]
      val service = new SmsServiceImpl(mockRepoSubscription, mockRepoResponse, mockRepoRequest, mockRepoTopicStats, mockRepoWindowClose, mockProvider)

      // When
      when(mockRepoSubscription.findById(subscriptionId)).thenReturn(None)
      val response = service.getSubscription(subscriptionId)

      // Then
      verify(mockRepoSubscription, times(1)).findById(subscriptionId)
      response mustEqual None
    }

    "return a List of subscriptions for a known subscription id" in {
      val subscriptionId = UUID.randomUUID().toString
      val mockRepoSubscription = mock[SubscriptionRepository]
      val mockRepoResponse = mock[ResponseRepository]
      val mockProvider = mock[SmsProvider]
      val mockRepoRequest = mock[RequestRepository]
      val mockRepoTopicStats = mock[TopicStatsRepository]
      val mockRepoWindowClose = mock[WindowCloseRepository]
      val service = new SmsServiceImpl(mockRepoSubscription, mockRepoResponse, mockRepoRequest, mockRepoTopicStats, mockRepoWindowClose, mockProvider)

      val subscription = SubscriptionBuilder().withRandomValues.withId(subscriptionId).build

      // When
      when(mockRepoSubscription.findById(subscriptionId)).thenReturn(Some(subscription))
      val response = service.getSubscription(subscriptionId)

      // Then
      verify(mockRepoSubscription, times(1)).findById(subscriptionId)
      response.size mustEqual 1
    }
  }

  "#subscriptionsByPhone" should {
    "return an empty List of subscriptions for an unknown phone number" in {
      val phoneNum = "4156963449"
      val mockRepoSubscription = mock[SubscriptionRepository]
      val mockRepoResponse = mock[ResponseRepository]
      val mockProvider = mock[SmsProvider]
      val mockRepoRequest = mock[RequestRepository]
      val mockRepoTopicStats = mock[TopicStatsRepository]
      val mockRepoWindowClose = mock[WindowCloseRepository]
      val service = new SmsServiceImpl(mockRepoSubscription, mockRepoResponse, mockRepoRequest, mockRepoTopicStats, mockRepoWindowClose, mockProvider)

      // When
      when(mockRepoSubscription.findByPhoneNumber(phoneNum)).thenReturn(Seq())
      val response = service.subscriptionsByPhoneNumber(phoneNum)

      // Then
      verify(mockRepoSubscription, times(1)).findByPhoneNumber(phoneNum)
      response.size mustEqual 0
    }

    "return a List of subscriptions for a known phone number" in {
      val phoneNum = "4156963449"
      val mockRepoSubscription = mock[SubscriptionRepository]
      val mockRepoResponse = mock[ResponseRepository]
      val mockProvider = mock[SmsProvider]
      val mockRepoRequest = mock[RequestRepository]
      val mockRepoTopicStats = mock[TopicStatsRepository]
      val mockRepoWindowClose = mock[WindowCloseRepository]
      val service = new SmsServiceImpl(mockRepoSubscription, mockRepoResponse, mockRepoRequest, mockRepoTopicStats, mockRepoWindowClose, mockProvider)

      // When
      when(mockRepoSubscription.findByPhoneNumber(phoneNum)).thenReturn(Seq(new Subscription()))
      val response = service.subscriptionsByPhoneNumber(phoneNum)

      // Then
      verify(mockRepoSubscription, times(1)).findByPhoneNumber(phoneNum)
      response.size mustEqual 1
    }

  }

  "#verify subscription" should {
    "Service updates 1 subscription for valid phone number" in {
      val phoneNum = "4155551212"
      val longCode = UUID.randomUUID().toString

      val mockRepoSubscription = mock[SubscriptionRepository]
      val mockRepoResponse = mock[ResponseRepository]
      val mockProvider = mock[SmsProvider]
      val mockRepoRequest = mock[RequestRepository]
      val mockRepoTopicStats = mock[TopicStatsRepository]
      val mockRepoWindowClose = mock[WindowCloseRepository]
      val service = new SmsServiceImpl(mockRepoSubscription, mockRepoResponse, mockRepoRequest, mockRepoTopicStats, mockRepoWindowClose, mockProvider)

      val phoneNumValidation = new PhoneNumberValidation {}
      val formattedPhoneNumber: String = phoneNumValidation.getFormattedPhoneNumber(phoneNum)

      // When
      when(mockRepoSubscription.updateStateByLongCode(longCode, formattedPhoneNumber, SubscriptionState.SUBSCRIBED)).thenReturn(1)
      val response = service.verify(SubscriptionBuilder(phoneNumber = phoneNum, longCode = longCode).build)

      // Then
      verify(mockRepoSubscription, times(1)).updateStateByLongCode(longCode, formattedPhoneNumber, SubscriptionState.SUBSCRIBED)
      response.code mustEqual "ok"
    }

    "Service updates 0 subscriptions for a NULL phone number" in {
      val phoneNum = null

      val mockRepoSubscription = mock[SubscriptionRepository]
      val mockRepoResponse = mock[ResponseRepository]
      val mockProvider = mock[SmsProvider]
      val mockRepoRequest = mock[RequestRepository]
      val mockRepoTopicStats = mock[TopicStatsRepository]
      val mockRepoWindowClose = mock[WindowCloseRepository]
      val service = new SmsServiceImpl(mockRepoSubscription, mockRepoResponse, mockRepoRequest, mockRepoTopicStats, mockRepoWindowClose, mockProvider)

      // When
      var catchException = false
      try {
        val updated = service.verify(phoneNum)
      }
      catch {
        case e: NullPointerException => catchException = true
      }

      //Then
      catchException mustEqual true
    }
  }

  "#create response " should {

    "create a response for a subscription " in {

      val phoneNumber: String = "8005551210"
      val subsId = UUID.randomUUID().toString
      val blahUserId: String = UUID.randomUUID().toString
      val blahMsgBody: String = "msg_body"
      val longCode: String = UUID.randomUUID.toString
      val blahTopic = "some_topic"

      val incomingRequest: IncomingSMSRequest = IncomingSMS(phoneNumber = phoneNumber, longCode = longCode, msgBody = blahMsgBody)

      val subscription: Subscription = SubscriptionBuilder()
        .withPhoneNumber(phoneNumber)
        .withTopic(blahTopic)
        .withUserId(blahUserId)
        .withState(SubscriptionState.SUBSCRIBED)
        .withLongCode(longCode)
        .build

      val mockRepoSubscription = mock[SubscriptionRepository]
      val mockRepoResponse = mock[ResponseRepository]
      val mockProvider = mock[SmsProvider]
      val mockRepoRequest = mock[RequestRepository]
      val mockRepoTopicStats = mock[TopicStatsRepository]
      val mockRepoWindowClose = mock[WindowCloseRepository]
      val service = new SmsServiceImpl(mockRepoSubscription, mockRepoResponse, mockRepoRequest, mockRepoTopicStats, mockRepoWindowClose, mockProvider)

      // When
      when(mockRepoSubscription.findByLongCodePhoneNumber(longCode, phoneNumber)).thenReturn(Some(subscription))

      when(mockRepoResponse.create(subsId, blahMsgBody)).thenReturn(0L)

      val response = service.handleIncoming(subscription, blahMsgBody)


      // Then
      verify(mockRepoResponse, times(2)).create(subsId, blahMsgBody)

      response.code mustEqual "ok"

    }

  }

  "#POST send broadcast" should {
    "return ok on valid Broadcast request" in {
      val mockRepoSubscription = mock[SubscriptionRepository]
      val mockRepoResponse = mock[ResponseRepository]
      val mockProvider = mock[SmsProvider]
      val mockRepoRequest = mock[RequestRepository]
      val mockRepoTopicStats = mock[TopicStatsRepository]
      val mockRepoWindowClose = mock[WindowCloseRepository]
      val service = new SmsServiceImpl(mockRepoSubscription, mockRepoResponse, mockRepoRequest, mockRepoTopicStats, mockRepoWindowClose, mockProvider)

      // When
      val testTopic:String = "test-topic"
      val user1:String = "user1"
      val user2:String = "user2"
      val user3:String = "user3"
      val testUsers:List[String] = List(user1, user2, user3)

      val broadcast:Broadcast = BroadcastBuilder()
        .withMsgBody("test body")
        .withTopic(testTopic)
        .withUserIds(testUsers)
        .build
      val subscription1: Subscription = SubscriptionBuilder()
        .withPhoneNumber("4155551212")
        .withTopic(testTopic)
        .withUserId(user1)
        .withState(SubscriptionState.SUBSCRIBED)
        .withLongCode("4154453456")
        .build

      val subscription2: Subscription = SubscriptionBuilder()
        .withPhoneNumber("4085551212")
        .withTopic(testTopic)
        .withUserId(user2)
        .withState(SubscriptionState.SUBSCRIBED)
        .withLongCode("4153454567")
        .build

      val subscription3: Subscription = SubscriptionBuilder()
        .withPhoneNumber("9195551212")
        .withTopic(testTopic)
        .withUserId(user3)
        .withState(SubscriptionState.UNSUBSCRIBED)
        .build

      when(mockRepoSubscription.findByUserIdAndTopicSubscribed(user1, testTopic)).thenReturn(Seq(subscription1))
      when(mockRepoSubscription.findByUserIdAndTopicSubscribed(user2, testTopic)).thenReturn(Seq(subscription2))
      when(mockRepoSubscription.findByUserIdAndTopicSubscribed(user3, testTopic)).thenReturn(Seq()) // DB should not return since not SUBSCRIBED
      val sendRequest1 = SendRequest(subscription1.id, subscription1.longCode, subscription1.phoneNumber, broadcast.msgBody)
      val sendRequest2 = SendRequest(subscription2.id, subscription2.longCode, subscription2.phoneNumber, broadcast.msgBody)
      val sendRequest3 = SendRequest(subscription3.id, subscription3.longCode, subscription3.phoneNumber, broadcast.msgBody)
      val response:SmsServiceResponse = service.send(broadcast)

      // Then
      verify(mockRepoSubscription, times(1)).findByUserIdAndTopicSubscribed(user1, testTopic)
      verify(mockRepoSubscription, times(1)).findByUserIdAndTopicSubscribed(user2, testTopic)
      verify(mockRepoSubscription, times(1)).findByUserIdAndTopicSubscribed(user3, testTopic)
      verify(mockProvider, times(1)).send(_, _)
      verify(mockProvider, times(1)).send(_, _)
      verify(mockProvider, times(0)).send(_, _)
      response.code mustEqual "ok"
      response.causes.get.size mustEqual 2
    }

    "return fail on empty Broadcast request" in {
      val mockRepoSubscription = mock[SubscriptionRepository]
      val mockRepoResponse = mock[ResponseRepository]
      val mockProvider = mock[SmsProvider]
      val mockRepoRequest = mock[RequestRepository]
      val mockRepoTopicStats = mock[TopicStatsRepository]
      val mockRepoWindowClose = mock[WindowCloseRepository]
      val service = new SmsServiceImpl(mockRepoSubscription, mockRepoResponse, mockRepoRequest, mockRepoTopicStats, mockRepoWindowClose, mockProvider)

      // When
      val broadcast = BroadcastBuilder().build
      val response:SmsServiceResponse = service.send(broadcast)

      // Then
      response.code mustEqual "fail"
      response.causes.get.size mustEqual 1
    }
  }

}
