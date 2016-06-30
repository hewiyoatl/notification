package com.loyal3.sms.service

import java.sql.{SQLIntegrityConstraintViolationException, Timestamp}

import com.loyal3.sms.core._
import com.loyal3.sms.core.exceptions.{InvalidPhoneNumberException, MsgBodyOrTopicNotPresentException}
import com.loyal3.sms.core.util.ResourceBundles
import com.loyal3.sms.core.validators.{PhoneNumberValidation, SubscriptionValidation}
import com.loyal3.sms.service.provider.{SendRequest, SmsProvider, TwilioSendResponse}
import com.loyal3.sms.service.repository._
import org.apache.log4j.Logger
import org.skife.jdbi.v2.exceptions.DBIException

import scala.collection.mutable.ListBuffer

class SmsServiceImpl(subscriptionRepository: SubscriptionRepository = RepoManager.subscriptionRepository,
                     responseRepository: ResponseRepository = RepoManager.responseRepository,
                     requestRepository: RequestRepository = RepoManager.requestRepository,
                     topicStatsRepository: TopicStatsRepository = RepoManager.topicStatsRepository,
                     windowCloseRepository: WindowCloseRepository = RepoManager.windowCloseRepository,
                     provider: SmsProvider) extends SmsService with PhoneNumberValidation with SubscriptionValidation  {



  val logger:Logger = Logger.getLogger(getClass)

  def subscribe(request: SmsServiceRequest): SmsServiceResponse = {
    logger.debug("Subscribe")
    val errorArray = isSubscriptionValid(request.topic, request.userId, request.phoneNumber)

    if (errorArray.size == 0) {
      val formattedPhoneNumber: String = getFormattedPhoneNumber(request.phoneNumber)

      var subscriptionId: String = ""

      try {
        subscriptionRepository.isDuplicatedSubscription(Subscription(topic = request.topic, userId = request.userId, phoneNumber = formattedPhoneNumber)) match {
          case true => SmsServiceResponse("duplicate")
          case _ => {
            val fromNumber = provider.getPhoneNumber(Subscription(topic = request.topic))

            // 1. first still create the subscription record anyway, so that if failed we can try to handle it later
            val subscription: Subscription = Subscription(phoneNumber = formattedPhoneNumber,
              topic = request.topic, userId = request.userId, longCode = fromNumber.getOrElse(""), offerName = request.offerName)
            subscriptionId = subscriptionRepository.create(subscription)

            fromNumber match{
              case Some(validFromNumber) =>
                //Hewi / Kevin: Currently, subscribing multiple times would send SMS message
                //Move the two statements outside the catch blcok if business requires re-sending
                val initialMessage = SendRequest(subscriptionId, fromPhone = validFromNumber, phoneNumber = formattedPhoneNumber,
                  message = ResourceBundles.Messages.Subscribe.getString(request.offerName))

                val requestData: Request = Request(Option(subscriptionId), Option(ResourceBundles.Messages.Subscribe.getString(request.offerName)), OutgoingMessageType.SUBSCRIBE, fromNumber)
                requestRepository.create(requestData)
                provider.send(initialMessage, updateMessageStatus) // asynchronous

                SmsServiceResponse("ok", subscriptionId = Some(subscriptionId))
              case None =>
                val e = new Exception(s"SmsServiceImpl.subscribe did not send out a message because we failed to get a valid phone number, subscriptionId=${subscriptionId}")
                logger.error("", e)

                throw e
            }

          }
        }
      } catch {
        case e: DBIException => {
          e.getCause match {
            case sicve: SQLIntegrityConstraintViolationException => {
              logger.warn("user (id: " + request.userId + ") with same phone number is already subscribed with topic: " + request.topic + "; message: " + sicve.getMessage)
              SmsServiceResponse("duplicate")
            }
            case _ => {
              logger.error("SUBSCRIPTION ERROR: %s, Exception: %s".format(request.toString, e.getMessage))
              throw e
            }
          }
        }
      }
    } else {
      SmsServiceResponse("failed", causes = Some(errorArray))
    }
  }

  def resubscribe(request: SmsServiceRequest): SmsServiceResponse = {
    logger.info("Resubscribe")

    def sendResponse(subscription: Subscription) = {
      val subscriptionId = subscription.id
      val fromNumber = subscription.longCode
      val offerName = subscription.offerName
      val phoneNumber = subscription.phoneNumber

      val requestData = Request(
        Option(subscriptionId),
        Option(ResourceBundles.Messages.Subscribe.getString(offerName)),
        OutgoingMessageType.SUBSCRIBE, Option(fromNumber))

      requestRepository.create(requestData)

      val initialMessage = SendRequest(subscriptionId, fromPhone = fromNumber, phoneNumber = phoneNumber,
        message = ResourceBundles.Messages.Subscribe.getString(offerName))

      provider.send(initialMessage, updateMessageStatus) // asynchronous

      SmsServiceResponse("ok", subscriptionId = Some(subscriptionId))
    }

    val errorArray = isSubscriptionValid(request.topic, request.userId, request.phoneNumber)
    if (errorArray.size != 0) return SmsServiceResponse("failed", causes = Some(errorArray))

    val formattedPhoneNumber: String = getFormattedPhoneNumber(request.phoneNumber)

    val subscription = subscriptionRepository.findByUserIdTopicAndPhoneNumber(request.userId, request.topic, formattedPhoneNumber)
    subscription match {
      case Some(s) =>
        s.state match {
          case SubscriptionState.SUBSCRIBED => SmsServiceResponse("already_subscribed")
          case SubscriptionState.UNSUBSCRIBED =>
            val fromNumber = provider.getPhoneNumber(Subscription(topic = request.topic))

            // update long code and subscription state to SUBSCRIBED
            subscriptionRepository.updateStateLongCodeBySubsId(s.id, SubscriptionState.SUBSCRIBED, fromNumber.getOrElse(""))

            fromNumber match {
              case Some(validPhoneNumber) =>
                logger.info(s"Updated subscription ${s.id} to `subscribed` with long code ${fromNumber}.")
                sendResponse(s.copy(longCode = validPhoneNumber, state = SubscriptionState.SUBSCRIBED))
              case None=>
                val e = new Exception(s" resubscribe did not send out a message because we failed to get a valid phone number, subscriptionId=${s.id}")
                logger.error("", e)
                throw e
            }

          case _ => SmsServiceResponse("invalid_subscription_state")
        }
      case None => SmsServiceResponse("not_subscribed")
    }
  }

  def unsubscribe(request: SmsServiceRequest): SmsServiceResponse = {
    logger.debug("Unsubscribing")
    val count = subscriptionRepository.updateStateByLongCode(request.longCode, request.phoneNumber, SubscriptionState.UNSUBSCRIBED)
    logger.debug("%s entries are unsubscribed".format(count))
    SmsServiceResponse("ok")
  }

  def subscriptionsByUser(userId: String): Seq[Subscription] = {
    val subscriptions = subscriptionRepository.findByUserId(userId)
    logger.debug("Found %s subscriptions by user id: %s".format(subscriptions.length, userId))

    subscriptions
  }

  def subscriptionsByPhoneNumber(phoneNumber: String): Seq[Subscription] = {
    try {
      val formattedPhoneNumber: String = getFormattedPhoneNumber(phoneNumber)

      val subscriptions = subscriptionRepository.findByPhoneNumber(formattedPhoneNumber)
      logger.debug("Found %s subscriptions".format(subscriptions.length))

      subscriptions
    }
    catch {
      case e: InvalidPhoneNumberException => {
        logger.error("User id: " + phoneNumber, e)
        Seq()
      }
    }
  }

  def subscriptionsByTopic(topic: String, statusFilter: Option[String] = None): Seq[Subscription] = {
    val subscriptions = subscriptionRepository.findByTopic(topic, statusFilter)
    logger.debug("Found %s subscriptions by topic : %s".format(subscriptions.length, topic))
    subscriptions
  }

  def responsesByTopic(topic: String, startDate: Long, endDate: Long, startSeq: Long, endSeq: Long): Seq[IncomingSMS] = {
    val responses = responseRepository.findByTopic(topic, startDate, endDate, startSeq, endSeq)
    responses
  }

  def releaseProviderPhoneNumbers(topic: String): Boolean = {
    val subscriptions = subscriptionRepository
      .findByTopic(topic)
      .filterNot(_.state == SubscriptionState.TERMINATED)

    logger.info(s"Release twilio numbers for ${subscriptions.length} subscriptions on topic $topic")

    val longCodes = subscriptions
      .map(_.longCode)
      .distinct

    logger.info(s"Long codes to release: ${longCodes.length}")

    var released = 0

    longCodes.foreach {
      lc =>
        if(provider.releasePhoneNumber(lc)){
          released += 1
          subscriptions
            .filter(s => s.longCode == lc && s.state != SubscriptionState.TERMINATED&& s.state != SubscriptionState.UNSUBSCRIBED)
            .foreach(p => subscriptionRepository
              .updateStateByLongCode(lc, p.phoneNumber, SubscriptionState.TERMINATED))
        }
    }

    logger.info(s"Purge DONE, $released longCodes released")
    released > 0
  }


  def getSubscription(subscriptionId: String): Option[Subscription] = {
    val subscription = subscriptionRepository.findById(subscriptionId)
    logger.debug("Subscription id: %s, subscription: %s".format(subscriptionId, subscription))
    subscription
  }

  def verify(request: SmsServiceRequest): SmsServiceResponse = {
    val longCode = request.longCode
    val phoneNumber = request.phoneNumber
    val updated = subscriptionRepository.updateStateByLongCode(longCode, phoneNumber, SubscriptionState.SUBSCRIBED)
    logger.debug("%s subscription(s) of phone number is now in %s".format(updated, SubscriptionState.SUBSCRIBED))
    SmsServiceResponse("ok")
  }

  def handleIncoming(request: Subscription, msgBody: String): IncomingSMSResponse = {
    responseRepository.create(request.id, msgBody)
    IncomingSMSResponse("ok")
  }

  def handleMessage(request: IncomingSMSRequest): Option[Subscription] = {
    val longCode = request.longCode
    val phoneNumber = request.phoneNumber
    val subscriptions = subscriptionRepository.findByLongCodePhoneNumber(longCode, phoneNumber)
    subscriptions match {
      case subscription if (subscriptions != null && subscriptions.size > 0) => subscription
      case _ => None
    }
  }

  def send(request: Broadcast): SmsServiceResponse = {
    logger.debug("Send")
    val errors: ListBuffer[String] = ListBuffer()
    var counterNotSent = 0
    var counterSent = 0

    try {
      if (request.msgBody == null || request.topic == null) {
        throw new MsgBodyOrTopicNotPresentException
      }

      val windowClose = WindowClose(offerId = request.topic, priceEndDate = new Timestamp(request.pricingDate))

      try {
        windowCloseRepository.create(windowClose)
      }
      catch {
        case e: DBIException => {
          e.getCause match {
            case sicve: SQLIntegrityConstraintViolationException => {
              windowCloseRepository.updatePriceEndDate(windowClose)
            }
            case _ => throw e
          }
        }
      }
      request.userIds.par.foreach (
        record => {
          val userId: String = record.trim
          val subscriptions: Seq[Subscription] = subscriptionRepository.findByUserIdAndTopicSubscribed(userId.trim, request.topic)
          subscriptions.length match {
            case l if (l > 0) => {
              val sendRequest = SendRequest(subscriptions.head.id, subscriptions.head.longCode, subscriptions.head.phoneNumber, request.msgBody)

              val messageRequest = Request(subsId = Option(subscriptions.head.id), msgBody = Option(request.msgBody), msgType = OutgoingMessageType.CONFIRM)
              requestRepository.create(messageRequest)
              provider.send(sendRequest, updateMessageStatus)
              counterSent = counterSent + 1
            }
            case _ => {
              counterNotSent = counterNotSent + 1
            }
          }

        }
        )
      errors += ResourceBundles.Messages.MessagesSent.getString format counterSent
      errors += ResourceBundles.Messages.MessagesNotSent.getString format counterNotSent
      SmsServiceResponse("ok", causes = Some(errors.toArray))
    }
    catch {
      case e: MsgBodyOrTopicNotPresentException => {
        logger.error("Error %s, Broadcast: %s".format(e.getMessage, request.toString))
        errors += ResourceBundles.Messages.ErrorTopicMsgBody.getString
        SmsServiceResponse("fail", Some(errors.toArray))
      }
      case e: Exception => {
        logger.error("Error %s, Broadcast: %s".format(e.getMessage, request.toString))
        errors += e.getMessage // "error"
        SmsServiceResponse("fail", Some(errors.toArray))
      }
    }
  }

  /**
   * Updates the message status for the first time the twilio api was called.
   */
  def updateMessageStatus(sendResponse: TwilioSendResponse): Unit = {
    requestRepository.updateMessageStatus(sendResponse.status, sendResponse.sms.getSid, sendResponse.subscriptionId)
  }

  def messageType(longCode: String): Option[String] = {
    subscriptionRepository.findCurrentMessageTypeByLongCode(longCode)
  }

  def updateMessageStatusByMessageId(messageId: String, messageStatus: String): Int = {
    requestRepository.updateMessageStatusByMessageId(messageStatus, messageId)
  }

  def getTopicStats(topic: String): TopicStats = topicStatsRepository.findByTopic(topic)

  def validateRequest(signature: String, url: String, params: Map[String,String]) = provider.validateRequest(signature,url,params)
}
