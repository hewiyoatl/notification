package com.loyal3.sms.service.repository

import com.loyal3.sms.test.support.scopes.SelfAwareSpecification

import com.loyal3.sms.core.{SubscriptionBuilder, IncomingSMSBuilder}
import java.util.{Calendar, UUID}
import org.skife.jdbi.v2.DBI
import com.loyal3.sms.service.repository.datamapper.{SubscriptionDataMapper, ResponseDataMapper}
import java.sql.Timestamp

class JdbiResponseRepositoryDatabaseSpec extends SelfAwareSpecification {
  sequential

  val dbi: DBI = RepoManager.dbi
  val mapperResponse: ResponseDataMapper = dbi.onDemand(classOf[ResponseDataMapper])
  val responseRepository = new JdbiResponseRepository(mapperResponse)
  val mapperSubscription: SubscriptionDataMapper = dbi.onDemand(classOf[SubscriptionDataMapper])
  val subscriptionRepository = new JdbiSubscriptionRepository(mapperSubscription)

  def cleanAll: Unit = {
    val handle = dbi.open()
    handle.execute("delete from requests")
    handle.execute("delete from subscriptions")
    handle.execute("delete from responses")
    handle.close()
  }

  "#create" should {

    "create a response" in {
      cleanAll

      // Given
      val incomingResponse = IncomingSMSBuilder().withRandomValues().build

      // When
      val id = responseRepository.create(incomingResponse.subsId, incomingResponse.msgBody)

      // Then
      id mustNotEqual ""
    }
  }

  "#findById" should {
    "retrieve responses records created via #create" in {
      cleanAll

      val userId = UUID.randomUUID().toString
      val phoneNum = "4155551212"
      val longCode = UUID.randomUUID().toString
      val subscription1 = SubscriptionBuilder().withRandomValues().withUserId(userId).withPhoneNumber(phoneNum).withLongCode(longCode).withTopic("topic1").build

      val incomingResponse = IncomingSMSBuilder().withRandomValues().build

      // When
      val subs1 = subscriptionRepository.create(subscription1)
      val id: Long = responseRepository.create(subs1, incomingResponse.msgBody)
      val actual = responseRepository.findById(id).get

      // Then
      actual.id mustEqual id
    }
  }

  "#findAll" should {
    "findAll should return all responses that were created" in {
      cleanAll

      // Given
      val userId = UUID.randomUUID().toString
      val phoneNum = "4155551212"
      val longCode = UUID.randomUUID().toString
      val subscription1 = SubscriptionBuilder().withRandomValues().withUserId(userId).withPhoneNumber(phoneNum).withLongCode(longCode).withTopic("topic1").build
      val response1 = IncomingSMSBuilder().withRandomValues().build
      val response2 = IncomingSMSBuilder().withRandomValues().build

      // When
      val subs1 = subscriptionRepository.create(subscription1)
      val id1 = responseRepository.create(subs1, response1.msgBody)
      val id2 = responseRepository.create(subs1, response2.msgBody)
      val allIds = responseRepository.findAll.map(_.id)

      // Then
      allIds.contains(id1) mustEqual true
      allIds.contains(id2) mustEqual true
    }
  }

  "#findByUserId" should {
    "return all responses for the given user" in {
      cleanAll

      // Given
      val userId = UUID.randomUUID().toString
      val otherUserId = UUID.randomUUID().toString

      val phoneNum = "4155551212"
      val longCodeOne = UUID.randomUUID().toString
      val longCodeTwo = UUID.randomUUID().toString
      val subscription1 = SubscriptionBuilder().withRandomValues().withUserId(userId).withPhoneNumber(phoneNum).withLongCode(longCodeOne).withTopic("topic1").build
      val subscription2 = SubscriptionBuilder().withRandomValues().withUserId(otherUserId).withPhoneNumber(phoneNum).withLongCode(longCodeTwo).withTopic("topic1").build
      val response1 = IncomingSMSBuilder().withRandomValues().withUserId(userId).withMsgBody("msg1").build
      val response2 = IncomingSMSBuilder().withRandomValues().withUserId(userId).withMsgBody("msg2").build

      val response3 = IncomingSMSBuilder().withRandomValues().withUserId(otherUserId).build

      // When
      val subs1 = subscriptionRepository.create(subscription1)
      val subs2 = subscriptionRepository.create(subscription2)
      val id1 = responseRepository.create(subs1, response1.msgBody)
      val id2 = responseRepository.create(subs1, response2.msgBody)
      val id3 = responseRepository.create(subs2, response3.msgBody)
      val allIds = responseRepository.findByUserId(userId).map(_.id)

      // Then
      allIds.size mustEqual 2
      allIds.contains(id1) mustEqual true
      allIds.contains(id2) mustEqual true
      allIds.contains(id3) mustEqual false
    }

    "return all responses for specific range dates and specific topic " in {

      cleanAll

      // Given
      val userId = UUID.randomUUID().toString
      val calEndDate: Calendar = Calendar.getInstance()
      val calStartDate: Calendar = Calendar.getInstance()
      calStartDate.add(Calendar.MONTH, -1)

      val phoneNum = "4155551212"
      val longCodeOne = UUID.randomUUID().toString
      val phoneNumTwo = "4155551213"
      val longCodeTwo = UUID.randomUUID().toString
      val subscription1 = SubscriptionBuilder().withRandomValues().withUserId(userId).withPhoneNumber(phoneNum).withLongCode(longCodeOne).withTopic("topic11").withCreatedAt(new Timestamp(calStartDate.getTimeInMillis)).build
      val subscription2 = SubscriptionBuilder().withRandomValues().withUserId(userId).withPhoneNumber(phoneNumTwo).withLongCode(longCodeTwo).withTopic("topic11").withCreatedAt(new Timestamp(calEndDate.getTimeInMillis)).build
      val response1 = IncomingSMSBuilder().withRandomValues().withUserId(userId).withMsgBody("msg1").build
      val response2 = IncomingSMSBuilder().withRandomValues().withUserId(userId).withMsgBody("msg2").build

      // When
      val subs1 = subscriptionRepository.create(subscription1)
      val subs2 = subscriptionRepository.create(subscription2)
      val id1 = responseRepository.create(subs1, response1.msgBody, Some(new Timestamp(calStartDate.getTimeInMillis)))
      val id2 = responseRepository.create(subs2, response2.msgBody, Some(new Timestamp(calEndDate.getTimeInMillis)))
      calStartDate.add(Calendar.DATE, -1)
      val allIds = responseRepository.findByTopic("topic11", calStartDate.getTimeInMillis, Long.MaxValue, 0, Long.MaxValue)

      // Then
      allIds.size mustEqual 2
    }

    "return all responses for specific range dates and specific topic " in {

      cleanAll

      // Given
      val userId = UUID.randomUUID().toString
      val calEndDate: Calendar = Calendar.getInstance()
      val calStartDate: Calendar = Calendar.getInstance()
      calStartDate.add(Calendar.MONTH, -1)

      val phoneNum = "4155551212"
      val longCodeOne = UUID.randomUUID().toString
      val phoneNumTwo = "4155551213"
      val longCodeTwo = UUID.randomUUID().toString
      val subscription1 = SubscriptionBuilder().withRandomValues().withUserId(userId).withPhoneNumber(phoneNum).withLongCode(longCodeOne).withTopic("topic22").withCreatedAt(new Timestamp(calStartDate.getTimeInMillis)).build
      val subscription2 = SubscriptionBuilder().withRandomValues().withUserId(userId).withPhoneNumber(phoneNumTwo).withLongCode(longCodeTwo).withTopic("topic22").withCreatedAt(new Timestamp(calEndDate.getTimeInMillis)).build
      val response1 = IncomingSMSBuilder().withRandomValues().withUserId(userId).withMsgBody("msg1").build
      val response2 = IncomingSMSBuilder().withRandomValues().withUserId(userId).withMsgBody("msg2").build

      // When
      val subs1 = subscriptionRepository.create(subscription1)
      val subs2 = subscriptionRepository.create(subscription2)
      val id1 = responseRepository.create(subs1, response1.msgBody, Some(new Timestamp(calStartDate.getTimeInMillis)))
      val id2 = responseRepository.create(subs2, response2.msgBody, Some(new Timestamp(calEndDate.getTimeInMillis)))
      calStartDate.add(Calendar.DATE, -1)
      calEndDate.add(Calendar.DATE, -3)
      val allIds = responseRepository.findByTopic("topic22", calStartDate.getTimeInMillis, calEndDate.getTimeInMillis, 0, Long.MaxValue)

      // Then
      allIds.size mustEqual 1
    }
  }
}