package com.loyal3.sms.service.repository

import java.sql.{SQLException, Timestamp}
import java.util.UUID

import com.loyal3.sms.core.util.healthcheck.HealthCheckUtils
import com.loyal3.sms.core.util.security.EncryptDecryptUtil
import com.loyal3.sms.core.{Subscription, SubscriptionState}
import com.loyal3.sms.service.repository.datamapper.SubscriptionDataMapper

import scala.collection.JavaConversions._

trait SubscriptionRepository {

  @throws(classOf[SQLException])
  def create(subscription: Subscription): String

  def findById(id: String): Option[Subscription]

  /**
   * Find all subscriptions of the user id
   *
   * @param userId
   * @return a sequence of subscriptions, empty sequence if nothing is found
   */
  def findByUserId(userId: String): Seq[Subscription]

  /**
   * Find all subscriptions of the topic
   *
   * @param topic
   * @param statusFilter optional filter term
   * @return a sequence of subscriptions, empty sequence if nothing is found
   */
  def findByTopic(topic: String, statusFilter: Option[String] = None): Seq[Subscription]

  /**
   * Find all subscriptions of the phone number
   *
   * @param phoneNumber
   * @return a sequence of subscriptions, empty sequence if nothing is found
   */
  def findByPhoneNumber(phoneNumber: String): Seq[Subscription]

  /**
   * Update subscription(s) state for the phone number
   *
   * @param phoneNumber
   * @param state
   * @return number of subscriptions being updated
   */
  def updateStateByPhoneNumber(phoneNumber:String, state: SubscriptionState): Int

  /**
   * Update subscription(s) state for the long code
   *
   * @param longCode
   * @param state
   * @return number of subscriptions being updated
   */
  def updateStateByLongCode(longCode:String, phoneNumber: String, state: SubscriptionState): Int

  /**
   *
   * @param subsId
   * @param longCode
   * @return
   */
  def updateLongCodeBySubsId(subsId:String, longCode: String): Int

  /**
    * Updates a subscription's state and long code by the subscription id.
    */
  def updateStateLongCodeBySubsId(subsId:String, state: SubscriptionState, longCode:String): Int

  /**
   *
   * @param userId
   * @param topic
   * @return
   */
  def findByUserIdAndTopic(userId: String, topic: String): Seq[Subscription]

  /**
    * Finds a subscription by user id, topic, and phone number.
    */
  def findByUserIdTopicAndPhoneNumber(userId: String, topic: String, phoneNumber: String): Option[Subscription]

  /**
   *
   * @param userId
   * @param topic
   * @return
   */
  def findByUserIdAndTopicSubscribed(userId: String, topic: String): Seq[Subscription]

  /**
   *
   * @param longCode
   * @return
   */
  def findByLongCodePhoneNumber(longCode: String, phoneNumber: String): Option[Subscription]

  /**
   *
   * @param longCode
   * @return
   */
  def findCurrentMessageTypeByLongCode(longCode: String): Option[String]

  /**
   *
   * @param subscription
   * @return
   */
  def isDuplicatedSubscription(subscription: Subscription): Boolean

}

class JdbiSubscriptionRepository(mapper: SubscriptionDataMapper) extends SubscriptionRepository {

  @throws(classOf[SQLException])
  def create(subscription: Subscription): String = {
    val now: Timestamp = new Timestamp(System.currentTimeMillis / 1000 * 1000)
    val subId = UUID.randomUUID().toString
    val digestPhoneNumber = EncryptDecryptUtil.digest(subscription.phoneNumber)
    val encryptedPhoneNumber = EncryptDecryptUtil.encrypt(subscription.phoneNumber)

    mapper.create(
      id = subId,
      userId = subscription.userId,
      phoneNumber = encryptedPhoneNumber,
      phoneDigest = digestPhoneNumber,
      createdAt = now,
      updatedAt = now,
      state = SubscriptionState.SUBSCRIBED.toString,
      topic = subscription.topic.toString,
      longCode = subscription.longCode,
      offerName = subscription.offerName
    )
    subId
  }

  def findById(id: String): Option[Subscription] = {
    Option(mapper.findById(id))
  }

  def findByUserId(userId: String): Seq[Subscription] = {
    val subscriptions = decryptPhoneNumbers(mapper.findByUserId(userId))
    if(subscriptions != null) asScalaBuffer(subscriptions) else Seq()
  }

  def findByTopic(topic: String, statusFilter: Option[String] = None): Seq[Subscription] = {
    val subscriptions = {
      statusFilter match {
        case Some(term) => decryptPhoneNumbers(mapper.findByTopicAndStatus(topic, term))
        case None => decryptPhoneNumbers(mapper.findByTopic(topic))
      }
    }

    if(subscriptions != null) asScalaBuffer(subscriptions) else Seq()
  }

  def findByPhoneNumber(phoneNumber: String): Seq[Subscription] = {
    val digestPhoneNumber = EncryptDecryptUtil.digest(phoneNumber)
    val subscriptions = decryptPhoneNumbers(mapper.findByPhoneNumber(digestPhoneNumber))
    if(subscriptions != null) asScalaBuffer(subscriptions) else Seq()
  }

  def updateStateByPhoneNumber(phoneNumber:String, state: SubscriptionState): Int = {
    val digestPhoneNumber = EncryptDecryptUtil.digest(phoneNumber)
    mapper.updateStateByPhoneNumber(digestPhoneNumber, state.toString)
  }

  def updateStateByLongCode(longCode:String, phoneNumber: String, state: SubscriptionState): Int = {
    val digestPhoneNumber = EncryptDecryptUtil.digest(phoneNumber)
    mapper.updateStateByLongCode(longCode, digestPhoneNumber, state.toString)
  }

  private def decryptPhoneNumbers(subscriptions : java.util.List[Subscription]) = {
    val numbers : (String => String)  = EncryptDecryptUtil.decrypt(_)
    val modifiedSubscriptions = subscriptions.map(subscription => subscription.copy(phoneNumber = numbers(subscription.phoneNumber)))
    modifiedSubscriptions
  }

  def findByUserIdAndTopic(userId: String, topic: String): Seq[Subscription] = {
    val subscriptions = decryptPhoneNumbers(mapper.findByUserIdAndTopic(userId, topic))
    if(subscriptions != null) asScalaBuffer(subscriptions) else Seq()
  }

  def findByUserIdTopicAndPhoneNumber(userId: String, topic: String, phoneNumber: String): Option[Subscription] = {
    val digestPhoneNumber = EncryptDecryptUtil.digest(phoneNumber)
    val subscriptions = decryptPhoneNumbers(mapper.findByUserIdTopicAndPhoneNumber(userId, topic, digestPhoneNumber))
    if(subscriptions != null && subscriptions.nonEmpty) Some(subscriptions.head) else None
  }

  def findByUserIdAndTopicSubscribed(userId: String, topic: String): Seq[Subscription] = {
    val subscriptions = decryptPhoneNumbers(mapper.findByUserIdAndTopicStatus(userId, topic, SubscriptionState.SUBSCRIBED.toString))
    if(subscriptions != null) asScalaBuffer(subscriptions) else Seq()
  }

  def findByLongCodePhoneNumber(longCode: String, phoneNumber: String): Option[Subscription] = {
    val digestPhoneNumber = EncryptDecryptUtil.digest(phoneNumber)
    val subscriptions = decryptPhoneNumbers(mapper.findByLongCodePhoneNumber(longCode, digestPhoneNumber))
    if(subscriptions != null && subscriptions.nonEmpty) Some(subscriptions.head) else None
  }

  def updateLongCodeBySubsId(subsId:String, longCode: String): Int = {
    mapper.updateLongCodeBySubsId(subsId, longCode)
  }

  def updateStateLongCodeBySubsId(subsId: String, state: SubscriptionState, longCode: String): Int = {
    mapper.updateStateLongCodeBySubsId(subsId, state.toString, longCode)
  }

  def findCurrentMessageTypeByLongCode(longCode: String): Option[String] = {
    val lc = mapper.findCurrentMessageTypeByLongCode(longCode)
    if(lc != null) Some(lc) else None
  }

  def isDuplicatedSubscription(subscription: Subscription): Boolean = {
    val digestPhoneNumber = EncryptDecryptUtil.digest(subscription.phoneNumber)
    val isDuplicate = mapper.duplicateSubscription(subscription.userId, subscription.topic, digestPhoneNumber)
    if(isDuplicate == 1) true else false
  }

  private[repository] val healthUtils: HealthCheckUtils = HealthCheckUtils

}
