package com.loyal3.sms.service.repository

import java.sql.Timestamp

import com.loyal3.sms.core._
import com.loyal3.sms.service.repository.datamapper._
import com.loyal3.sms.test.support.scopes.SelfAwareSpecification
import org.skife.jdbi.v2.DBI

class JdbiTopicStatsRepositoryDatabaseSpec extends SelfAwareSpecification {
  sequential

  def now(plusSeconds: Int = 0): Timestamp = new Timestamp((System.currentTimeMillis / 1000 + plusSeconds) * 1000)

  val dbi: DBI = RepoManager.dbi
  val mapperRequest: RequestDataMapper = dbi.onDemand(classOf[RequestDataMapper])
  val requestRepository = new JdbiRequestRepository(mapperRequest)
  val mapperSubscription: SubscriptionDataMapper = dbi.onDemand(classOf[SubscriptionDataMapper])
  val subscriptionRepository = new JdbiSubscriptionRepository(mapperSubscription)
  val mapperTopicStats: TopicStatsDataMapper = dbi.onDemand(classOf[TopicStatsDataMapper])
  val topicStatsRepository = new JdbiTopicStatsRepository(mapperTopicStats)

  def cleanAll: Unit = {
    val handle = dbi.open()
    handle.execute("delete from requests")
    handle.execute("delete from subscriptions")
    handle.execute("delete from responses")
    handle.execute("delete from window_close")
    handle.close()
  }

  "get topic stats" should {
    "return zeroes if the topic is not there" in {
      cleanAll
      val missingTopic = "_missing_topic"
      val stats = topicStatsRepository.findByTopic(missingTopic)
      stats.topic mustEqual missingTopic
      stats.currentSubscriptions mustEqual 0
      stats.unsubscriptions mustEqual 0
      stats.deletedSubscriptions mustEqual 0
      stats.initialSubscriptions mustEqual 0
      stats.subscribeMessagesSent mustEqual 0
      stats.subscribeMessagesQueued mustEqual 0
      stats.confirmMessagesSent mustEqual 0
      stats.confirmMessagesQueued mustEqual 0
    }

    "counts the fields as intended" in {
      cleanAll
      val testTopic = "globant"

      val subscription1 = subscriptionRepository.create(Subscription(userId = "userId1", phoneNumber = "4150000001", topic = testTopic, offerName = "Ready", longCode = "4150000000"))
      subscriptionRepository.updateStateByPhoneNumber("4150000001",SubscriptionState.CREATED)
      val subscription2 = subscriptionRepository.create(Subscription(userId = "userId2", phoneNumber = "4150000002", topic = testTopic, offerName = "Ready", longCode = "4150000000"))
      val subscription3 = subscriptionRepository.create(Subscription(userId = "userId3", phoneNumber = "4150000003", topic = testTopic, offerName = "Ready", longCode = "4150000000"))
      subscriptionRepository.updateStateByPhoneNumber("4150000003", SubscriptionState.UNSUBSCRIBED)
      val subscription4 = subscriptionRepository.create(Subscription(userId = "userId4", phoneNumber = "4150000004", topic = testTopic, offerName = "Ready", longCode = "4150000000"))
      subscriptionRepository.updateStateByPhoneNumber("4150000004", SubscriptionState.DELETED)

      val msg1 = requestRepository.create(Request(subsId = Option(subscription1), msgBody = Option("Message1"), msgType = OutgoingMessageType.SUBSCRIBE))
      requestRepository.updateMessageStatus( "queued", msg1.toString, subscription1)
      val msg2 = requestRepository.create(Request(subsId = Option(subscription2), msgBody = Option("Message2"), msgType = OutgoingMessageType.SUBSCRIBE))
      requestRepository.updateMessageStatus("sent", msg2.toString, subscription2)
      val msg3 = requestRepository.create(Request(subsId = Option(subscription3), msgBody = Option("Message3"), msgType = OutgoingMessageType.SUBSCRIBE))
      requestRepository.updateMessageStatus("cancelled", msg3.toString, subscription3)
      requestRepository.create(Request(subsId = Option(subscription4), msgBody = Option("Message4"), msgType = OutgoingMessageType.SUBSCRIBE))
      val msgConfirm1 = requestRepository.create(Request(subsId = Option(subscription1), msgBody = Option("Message1"), msgType = OutgoingMessageType.CONFIRM))
      requestRepository.updateMessageStatus( "queued", msgConfirm1.toString, subscription1)
      val msgConfirm2 = requestRepository.create(Request(subsId = Option(subscription2), msgBody = Option("Message2"), msgType = OutgoingMessageType.CONFIRM))
      requestRepository.updateMessageStatus("sent", msgConfirm2.toString, subscription2)


      val stats = topicStatsRepository.findByTopic(testTopic)

      stats.topic mustEqual testTopic
      stats.currentSubscriptions mustEqual 1
      stats.unsubscriptions mustEqual 1
      stats.deletedSubscriptions mustEqual 1
      stats.initialSubscriptions mustEqual 4
      stats.subscribeMessagesSent mustEqual 1
      stats.subscribeMessagesQueued mustEqual 4
      stats.confirmMessagesSent mustEqual 1
      stats.confirmMessagesQueued mustEqual 2
    }

  }
}
