package com.loyal3.sms.service

import com.loyal3.sms.core._

trait SmsService {

  /**
   * Subscribe the given user to the given topic
   */
  def subscribe(request: SmsServiceRequest): SmsServiceResponse

  /**
    * Subscribes an unsubscribed user and assigns a new long code to the user
    */
  def resubscribe(request: SmsServiceRequest): SmsServiceResponse

    /**
   * Unsubscribe the given user to the given topic
   */
  def unsubscribe(request: SmsServiceRequest): SmsServiceResponse

  /**
   * Transitioning from CREATED to SUBSCRIBED
   * @return number of phone number updated
   */
  def verify(request: SmsServiceRequest): SmsServiceResponse

  /**
   * Gets the subscription from the given subscription id
   */
  def getSubscription(subscriptionId: String): Option[Subscription]

  /**
   * Gets all subscriptions by phoneNumber
   */
  def subscriptionsByPhoneNumber(phoneNumber: String): Seq[Subscription]

  /**
   * Gets all subscriptions for the given user
   */
  def subscriptionsByUser(userId: String): Seq[Subscription]

  /**
   * Gets all the subscriptions for the given topic
   */
  def subscriptionsByTopic(topic: String, statusFilter: Option[String] = None): Seq[Subscription]

  /**
   * Gets all the responses for the given topic
   */
  def responsesByTopic(topic: String, startDate: Long, endDate: Long, startSeq: Long, endSeq: Long): Seq[IncomingSMS]

  /**
    * Releases the phone numbers for the given topic
    */
  def releaseProviderPhoneNumbers(topic:String):Boolean

  /**
   * Save the incoming responses
   */
  def handleIncoming(request: Subscription, msgBody: String): IncomingSMSResponse

  /**
   * Sends a message to a group of users
   */
  def send(request: Broadcast): SmsServiceResponse

  def messageType(longCode: String): Option[String]

  def handleMessage(request: IncomingSMSRequest): Option[Subscription]

  def updateMessageStatusByMessageId(messageId: String, messageStatus: String): Int

  def getTopicStats(topic: String): TopicStats

  def validateRequest(signature: String, url: String, params: Map[String,String])

}
