package com.loyal3.sms.service.repository

import java.util.UUID
import com.loyal3.sms.core.{SubscriptionBuilder, SubscriptionState}
import com.loyal3.sms.service.repository.datamapper.{ResponseDataMapper, SubscriptionDataMapper}
import com.loyal3.sms.test.support.scopes.SelfAwareSpecification

import org.scalatest.mock.MockitoSugar
import org.skife.jdbi.v2.DBI
import org.specs2.specification.BeforeExample


class JdbiSubscriptionRepositoryDatabaseSpec
  extends SelfAwareSpecification
  with BeforeExample
  with MockitoSugar {
  sequential

  val dbi: DBI = RepoManager.dbi
  val mapperSubscription: SubscriptionDataMapper = dbi.onDemand(classOf[SubscriptionDataMapper])
  val subscriptionRepository = new JdbiSubscriptionRepository(mapperSubscription)

  val mapperResponse: ResponseDataMapper = dbi.onDemand(classOf[ResponseDataMapper])
  val responseRepository = new JdbiResponseRepository(mapperResponse)

  def before = cleanAll

  def cleanAll {
    val handle = dbi.open()
    handle.execute("delete from requests")
    handle.execute("delete from subscriptions")
    handle.execute("delete from responses")
    handle.close
  }

  "#create" should {
    "create a subscription" in {
      // Given
      val subscription = SubscriptionBuilder().withRandomValues().build

      // When
      val id = subscriptionRepository.create(subscription)

      // Then
      id mustNotEqual ""
    }
  }

  "#findById" should {
    "retrieve Subscription records created via #create" in {
      val subscription = SubscriptionBuilder().withRandomValues().build

      // When
      val id: String = subscriptionRepository.create(subscription)
      val actual = subscriptionRepository.findById(id).get

      // Then
      actual.id mustEqual id
    }
  }

  "#findByUserId" should {
    "return all subscriptions for the given user" in {
      // Given
      val userId = UUID.randomUUID().toString
      val subscription1 = SubscriptionBuilder().withRandomValues().withUserId(userId).withTopic("topic33").build
      val subscription2 = SubscriptionBuilder().withRandomValues().withUserId(userId).withTopic("topic44").build

      val otherUserId = UUID.randomUUID().toString
      val subscription3 = SubscriptionBuilder().withRandomValues().withUserId(otherUserId).build

      // When
      val id1 = subscriptionRepository.create(subscription1)
      val id2 = subscriptionRepository.create(subscription2)
      val id3 = subscriptionRepository.create(subscription3)
      val allIds = subscriptionRepository.findByUserId(userId).map(_.id)

      // Then
      allIds.size mustEqual 2
      allIds.contains(id1) mustEqual true
      allIds.contains(id2) mustEqual true
      allIds.contains(id3) mustEqual false
    }
  }

  "#findByTopic" should {
    "return all subscriptions for the given topic, not others" in {
      // Given
      val subscription1 = SubscriptionBuilder().withRandomValues().withUserId(UUID.randomUUID().toString).withTopic("test-topic").build
      val subscription2 = SubscriptionBuilder().withRandomValues().withUserId(UUID.randomUUID().toString).withTopic("test-topic").build
      val subscription3 = SubscriptionBuilder().withRandomValues().withUserId(UUID.randomUUID().toString).withTopic("test-topic").build

      // Other topic
      val subscription4 = SubscriptionBuilder().withRandomValues().withUserId(UUID.randomUUID().toString).withTopic("test-other-topic").build

      // When
      val id1 = subscriptionRepository.create(subscription1)
      val id2 = subscriptionRepository.create(subscription2)
      val id3 = subscriptionRepository.create(subscription3)
      val id4 = subscriptionRepository.create(subscription4)

      val allIds = subscriptionRepository.findByTopic("test-topic").map(_.id)

      // Then
      allIds.size mustEqual 3
      allIds.contains(id1) mustEqual true
      allIds.contains(id2) mustEqual true
      allIds.contains(id3) mustEqual true
      allIds.contains(id4) mustEqual false
    }

    "return all subscriptions for the given topic and SUBSCRIBED state, not others" in {
      val phoneNum = "4155551212"

      // Given
      val subscription1 = SubscriptionBuilder().withRandomValues().withUserId(UUID.randomUUID().toString).withTopic("test-topic").build
      val subscription2 = SubscriptionBuilder().withRandomValues().withUserId(UUID.randomUUID().toString).withTopic("test-topic").build
      val subscription3 = SubscriptionBuilder().withRandomValues().withUserId(UUID.randomUUID().toString).withPhoneNumber(phoneNum).
        withTopic("test-topic").build

      // Other topic
      val subscription4 = SubscriptionBuilder().withRandomValues().withUserId(UUID.randomUUID().toString).withTopic("test-other-topic").build

      // When
      val id1 = subscriptionRepository.create(subscription1)
      val id2 = subscriptionRepository.create(subscription2)
      val id3 = subscriptionRepository.create(subscription3)
      val id4 = subscriptionRepository.create(subscription4)

      // transition to SUBSCRIBED
      subscriptionRepository.updateStateByPhoneNumber(phoneNum, SubscriptionState.SUBSCRIBED)

      val allIds = subscriptionRepository.findByTopic("test-topic", Some("SUBSCRIBED")).map(_.id)

      // Then
      allIds.size mustEqual 3
      allIds.contains(id1) mustEqual true
      allIds.contains(id2) mustEqual true
      allIds.contains(id3) mustEqual true
      allIds.contains(id4) mustEqual false
    }

    "return all subscriptions for the given topic and UNSUBSCRIBED state, not others" in {
      val phoneNum = "4155551212"

      // Given
      val subscription1 = SubscriptionBuilder().withRandomValues().withUserId(UUID.randomUUID().toString).withTopic("test-topic").build
      val subscription2 = SubscriptionBuilder().withRandomValues().withUserId(UUID.randomUUID().toString).withTopic("test-topic").build
      val subscription3 = SubscriptionBuilder().withRandomValues().withUserId(UUID.randomUUID().toString).withPhoneNumber(phoneNum).
        withTopic("test-topic").build

      // Other topic
      val subscription4 = SubscriptionBuilder().withRandomValues().withUserId(UUID.randomUUID().toString).withTopic("test-other-topic").build

      // When
      val id1 = subscriptionRepository.create(subscription1)
      val id2 = subscriptionRepository.create(subscription2)
      val id3 = subscriptionRepository.create(subscription3)
      val id4 = subscriptionRepository.create(subscription4)

      // transition to SUBSCRIBED
      subscriptionRepository.updateStateByPhoneNumber(phoneNum, SubscriptionState.UNSUBSCRIBED)

      val allIds = subscriptionRepository.findByTopic("test-topic", Some("UNSUBSCRIBED")).map(_.id)

      // Then
      allIds.size mustEqual 1
      allIds.contains(id1) mustEqual false
      allIds.contains(id2) mustEqual false
      allIds.contains(id3) mustEqual true
      allIds.contains(id4) mustEqual false
    }
  }

  "#updateStateByLongCode" should {
    "verify all subscriptions to SUBSCRIBED based on phone number" in {
      val userId = UUID.randomUUID().toString
      val phoneNum = "0987654321"

      val otherUserId = UUID.randomUUID().toString
      val otherPhoneNum = "1234512345"
      // Given
      val subscription1 = SubscriptionBuilder().withRandomValues().withUserId(userId).withPhoneNumber(phoneNum).withTopic("topic55").build
      val subscription2 = SubscriptionBuilder().withRandomValues().withUserId(userId).withPhoneNumber(phoneNum).withTopic("topic66").build

      val subscription3 = SubscriptionBuilder().withRandomValues().withUserId(otherUserId).withPhoneNumber(otherPhoneNum).withTopic("topic55").build

      // When
      val id1 = subscriptionRepository.create(subscription1)
      val id2 = subscriptionRepository.create(subscription2)
      val id3 = subscriptionRepository.create(subscription3)
      val updated = subscriptionRepository.updateStateByPhoneNumber(phoneNum, SubscriptionState.SUBSCRIBED)

      // Then
      updated mustEqual 2

      val subscriptions = subscriptionRepository.findByPhoneNumber(phoneNum).map(_.id)
      subscriptions.size mustEqual 2

      subscriptions.contains(id1) mustEqual true
      subscriptions.contains(id2) mustEqual true
      subscriptions.contains(id3) mustEqual false
    }

  }

  "#findByUserIdAndTopicSubscribed" should {
    "return the subscriptions for the given userId, topic and SUBSCRIBED state, but not other states" in {
      val topic = "some-ipo"
      val userId1 = UUID.randomUUID().toString
      val userId2 = UUID.randomUUID().toString
      val userId3 = UUID.randomUUID().toString

      val phone1 = "4155551212"
      val phone2 = "4085551212"
      val phone3 = "9195551212"

      // Given
      val subscription1 = SubscriptionBuilder().withRandomValues().withUserId(userId1).withTopic(topic).withPhoneNumber(phone1).build
      val subscription2 = SubscriptionBuilder().withRandomValues().withUserId(userId2).withTopic(topic).withPhoneNumber(phone2).build
      val subscription3 = SubscriptionBuilder().withRandomValues().withUserId(userId3).withTopic(topic).withPhoneNumber(phone3).build

      // When
      val id1 = subscriptionRepository.create(subscription1)
      val id2 = subscriptionRepository.create(subscription2)
      val id3 = subscriptionRepository.create(subscription3)

      // transitions
      subscriptionRepository.updateStateByPhoneNumber(subscription1.phoneNumber, SubscriptionState.SUBSCRIBED)
      subscriptionRepository.updateStateByPhoneNumber(subscription2.phoneNumber, SubscriptionState.UNSUBSCRIBED)
      subscriptionRepository.updateStateByPhoneNumber(subscription3.phoneNumber, SubscriptionState.UNSUBSCRIBED)

      val allIds1 = subscriptionRepository.findByUserIdAndTopicSubscribed(userId1, topic).map(_.id)
      val allIds2 = subscriptionRepository.findByUserIdAndTopicSubscribed(userId2, topic).map(_.id)
      val allIds3 = subscriptionRepository.findByUserIdAndTopicSubscribed(userId3, topic).map(_.id)

      // Then
      allIds1.size mustEqual 1
      allIds1.contains(id1) mustEqual true

      allIds2.size mustEqual 0
      allIds2.contains(id2) mustEqual false

      allIds3.size mustEqual 0
      allIds3.contains(id3) mustEqual false
    }
  }
}
