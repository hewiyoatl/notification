package com.loyal3.sms.service.repository

import com.loyal3.sms.test.support.scopes.SelfAwareSpecification
import com.loyal3.sms.core.{IncomingSMSBuilder, SubscriptionBuilder}
import java.util.UUID
import org.skife.jdbi.v2.DBI
import com.loyal3.sms.service.repository.datamapper.{ResponseDataMapper, SubscriptionDataMapper}


class JdbiRepositoryDatabaseSpec extends SelfAwareSpecification {
  sequential

  val dbi: DBI = RepoManager.dbi
  val mapperSubscription: SubscriptionDataMapper = dbi.onDemand(classOf[SubscriptionDataMapper])
  val subscriptionRepository = new JdbiSubscriptionRepository(mapperSubscription)
  val mapperResponse: ResponseDataMapper = dbi.onDemand(classOf[ResponseDataMapper])
  val responseRepository = new JdbiResponseRepository(mapperResponse)

  def cleanAll: Unit = {
    val handle = dbi.open()
    handle.execute("delete from requests")
    handle.execute("delete from subscriptions")
    handle.execute("delete from responses")
    handle.close()
  }

  "#findByTopic" should {
    "findByTopic should return all responses for a given topic" in {
      cleanAll
      // Given
      val userId = UUID.randomUUID().toString
      val phoneNum = "4155551212"

      val subscription1 = SubscriptionBuilder().withRandomValues().withUserId(userId).withPhoneNumber(phoneNum).withTopic("topic1Repo").build
      val subscription2 = SubscriptionBuilder().withRandomValues().withUserId(userId).withPhoneNumber(phoneNum).withTopic("topic2Repo").build

      val response1 = IncomingSMSBuilder().withUserId(userId).withSubsId(subscription1.id).build
      val response2 = IncomingSMSBuilder().withUserId(userId).withSubsId(subscription2.id).build

      // When
      val subs1 = subscriptionRepository.create(subscription1)
      val subs2 = subscriptionRepository.create(subscription2)

      val id1 = responseRepository.create(subs1, response1.msgBody)
      val id2 = responseRepository.create(subs2, response2.msgBody)
      val allIds = responseRepository.findByTopic("topic1Repo", 0L, Long.MaxValue, 0L, Long.MaxValue).map(_.id)

      // Then
      allIds.contains(id1) mustEqual true
      allIds.contains(id2) mustEqual false
    }

    "findByTopic should return all responses for a given topic with timestamp " in {
      cleanAll
      // Given
      val userId = UUID.randomUUID().toString
      val phoneNum = "4155551212"

      val subscription1 = SubscriptionBuilder().withRandomValues().withUserId(userId).withPhoneNumber(phoneNum).withTopic("topic3Repo").build
      val subscription2 = SubscriptionBuilder().withRandomValues().withUserId(userId).withPhoneNumber(phoneNum).withTopic("topic4Repo").build

      val response1 = IncomingSMSBuilder().withUserId(userId).withSubsId(subscription1.id).build
      val response2 = IncomingSMSBuilder().withUserId(userId).withSubsId(subscription2.id).build

      // When
      val subs1 = subscriptionRepository.create(subscription1)
      val subs2 = subscriptionRepository.create(subscription2)

      val id1 = responseRepository.create(subs1, response1.msgBody)
      val id2 = responseRepository.create(subs2, response2.msgBody)
      val allIds = responseRepository.findByTopic("topic3Repo", 2051251200000L, Long.MaxValue, 0L, Long.MaxValue).map(_.id)

      // Then
      allIds.size mustEqual 0
    }

    "findByTopic should return all responses for a given topic with timestamp and sequences " in {
      cleanAll
      // Given
      val userId = UUID.randomUUID().toString
      val phoneNum = "4155551212"

      val subscription1 = SubscriptionBuilder().withRandomValues().withUserId(userId).withPhoneNumber(phoneNum).withTopic("topic3Repo").build

      val response1 = IncomingSMSBuilder().withUserId(userId).withSubsId(subscription1.id).build

      // When
      val subs1 = subscriptionRepository.create(subscription1)

      val id1 = responseRepository.create(subs1, response1.msgBody)
      val id2 = responseRepository.create(subs1, response1.msgBody)
      val allIds = responseRepository.findByTopic("topic3Repo", 0L, Long.MaxValue, id1, id2).map(_.id)

      // Then
      allIds.size mustEqual 2
    }

    "findByTopic should return all responses for a given topic with timestamp and sequences " in {
      cleanAll
      // Given
      val userId = UUID.randomUUID().toString
      val phoneNum = "4155551212"

      val subscription = SubscriptionBuilder().withRandomValues().withUserId(userId).withPhoneNumber(phoneNum).withTopic("topic4Repo").build

      val response = IncomingSMSBuilder().withUserId(userId).withSubsId(subscription.id).build

      // When
      val subs = subscriptionRepository.create(subscription)

      val id1 = responseRepository.create(subs, response.msgBody)
      val id2 = responseRepository.create(subs, response.msgBody)
      val id3 = responseRepository.create(subs, response.msgBody)
      val id4 = responseRepository.create(subs, response.msgBody)

      val allIdsRange1 = responseRepository.findByTopic("topic4Repo", 0L, Long.MaxValue, id1, id3).map(_.id)
      val allIdsRange2 = responseRepository.findByTopic("topic4Repo", 0L, Long.MaxValue, id4, id4).map(_.id)

      // Then
      allIdsRange1.size mustEqual 3
      allIdsRange2.size mustEqual 1
    }


  }

}
