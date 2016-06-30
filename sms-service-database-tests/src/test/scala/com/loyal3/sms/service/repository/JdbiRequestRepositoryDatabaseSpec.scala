package com.loyal3.sms.service.repository

import com.loyal3.sms.test.support.scopes.SelfAwareSpecification
import org.skife.jdbi.v2.DBI
import com.loyal3.sms.service.repository.datamapper.{SubscriptionDataMapper, RequestDataMapper}
import com.loyal3.sms.core.{OutgoingMessageType, SubscriptionBuilder, RequestBuilder}
import java.util.UUID

/**
 * Created with IntelliJ IDEA.
 * User: hewi
 * Date: 4/7/14
 * Time: 12:05 PM
 * To change this template use File | Settings | File Templates.
 */
class JdbiRequestRepositoryDatabaseSpec extends SelfAwareSpecification {
  sequential

  val dbi: DBI = RepoManager.dbi

  val mapperSubscription: SubscriptionDataMapper = dbi.onDemand(classOf[SubscriptionDataMapper])
  val subscriptionRepository = new JdbiSubscriptionRepository(mapperSubscription)

  val requestMapper: RequestDataMapper = dbi.onDemand(classOf[RequestDataMapper])
  val requestRepository = new JdbiRequestRepository(requestMapper)

  def cleanAll: Unit = {
    val handle = dbi.open()
    handle.execute("delete from requests")
    handle.execute("delete from subscriptions")
    handle.close
  }

  "#create" should {
    "create a requests record" in {
      cleanAll
      // Given
      val subscription = SubscriptionBuilder().withRandomValues().build

      // When
      val subsId: String = subscriptionRepository.create(subscription)
      val requests = RequestBuilder().withRandomValues().withSubsId(subsId).build
      val id = requestRepository.create(requests)

      // Then
      id mustNotEqual ""
    }
  }

  "#update" should {
    "update message status of last record" in {
      cleanAll

      // Given
      val subscription1 = SubscriptionBuilder().withRandomValues().build

      // When
      val subsId1: String = subscriptionRepository.create(subscription1)

      val request1 = RequestBuilder().withRandomValues().withSubsId(subsId1).build
      requestRepository.create(request1)

      val request2 = RequestBuilder().withRandomValues().withSubsId(subsId1).withMsgType(OutgoingMessageType.CONFIRM).build
      requestRepository.create(request2)

      val updatedRows = requestRepository.updateMessageStatus("delivered", "22132414", subsId1)

      // Then
      updatedRows mustEqual 1
    }

    "update message status by message id" in {
      cleanAll

      // Given
      val subscription1 = SubscriptionBuilder().withRandomValues().build

      // When
      val subsId1: String = subscriptionRepository.create(subscription1)

      val request1 = RequestBuilder().withRandomValues().withSubsId(subsId1).build
      requestRepository.create(request1)

      val request2 = RequestBuilder().withRandomValues().withSubsId(subsId1).withMsgType(OutgoingMessageType.CONFIRM).build
      requestRepository.create(request2)

      val messageId = UUID.randomUUID.toString
      requestRepository.updateMessageStatus("delivered", messageId, subsId1)

      val updatedRows = requestRepository.updateMessageStatusByMessageId("submitted", messageId)

      // Then
      updatedRows mustEqual 1
    }

  }

}
