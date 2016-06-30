package com.loyal3.sms.service.controller.twilio

import java.sql.Timestamp
import java.util.Date

import com.loyal3.sms.config.Config
import com.loyal3.sms.core.exceptions.InvalidPhoneNumberException
import com.loyal3.sms.core.util.ResourceBundles
import com.loyal3.sms.core.validators.PhoneNumberValidation
import com.loyal3.sms.core.{IncomingSMS, OutgoingMessageType, Subscription}
import com.loyal3.sms.service.SmsService
import com.twitter.finatra.{Request, Controller}
import org.apache.commons.lang.StringEscapeUtils


class TwilioController(smsService: SmsService) extends Controller with PhoneNumberValidation {

  /**
    * Takes care of all incoming messages from Twilio
    */
  post("/api/sms/callback/twilio/incoming") {
    request =>
      try {
        validateRequest(request, Config.current.provider.smsURL)

        var response: String = null
        val incomingLongCode = request.params.getOrElse("To", "")

        def isWindowClosed(subscription: Option[Subscription], today: Timestamp): Boolean = subscription match {
          case Some(d) => {
            if (d.priceEndDate != null && d.priceEndDate.before(today))
              true
            else
              false
          }
          case _ => false
        }

        (request.params.get("From"), request.params.get("Body")) match {

          case (Some(fromPhoneNumber), Some(msg)) => {

            val formattedPhoneNumber: String = getFormattedPhoneNumber(fromPhoneNumber)

            log.info("/api/sms/callback/twilio/incoming called with From,To,Body: %s,%s,%s".
              format(fromPhoneNumber, incomingLongCode, msg))

            val now: Timestamp = new Timestamp(System.currentTimeMillis / 1000 * 1000)

            val subscription = smsService.handleMessage(IncomingSMS(phoneNumber = formattedPhoneNumber, longCode = incomingLongCode, msgBody = msg.trim))
            val messageType = smsService.messageType(incomingLongCode)
            val smallMsg = msg.trim.toLowerCase

            if (isWindowClosed(subscription, now)) {
              log.info("RESPONSE AFTER WINDOW CLOSED - From,To,Body: %s,%s,%s".
                format(fromPhoneNumber, incomingLongCode, msg))
              if (OutgoingMessageType.CONFIRM.toString.equalsIgnoreCase(messageType.getOrElse(""))
                && (isConfirmMsg(smallMsg))) {
                smsService.handleIncoming(subscription.get, msg)
              }
              response = ResourceBundles.Messages.ConfirmAfterWindowClose.getString(subscription.get.offerName)
            } else {
              messageType match {
                case mType if mType.isDefined && (mType.get.toString == OutgoingMessageType.CONFIRM.toString) => {
                  if (isConfirmMsg(smallMsg)) {
                    log.info("CONFIRM|CONFIRMED RESPONSE - From,To,Body: %s,%s,%s".
                      format(fromPhoneNumber, incomingLongCode, msg))
                    smsService.handleIncoming(subscription.get, msg)
                    response = ResourceBundles.Messages.Confirm.getString(subscription.get.offerName)
                  }
                  else {
                    log.info("INVALID CONFIRM RESPONSE - From,To,Body: %s,%s,%s".
                      format(fromPhoneNumber, incomingLongCode, msg))
                    response = ResourceBundles.Messages.ConfirmInvalid.getString
                  }
                }
                case mType if mType.isDefined && (mType.get.toString == OutgoingMessageType.SUBSCRIBE.toString) => {
                  smallMsg match {
                    case "yes" | "start" | "subscribe" => {
                      log.info("YES|START|SUBSCRIBE RESPONSE - From,To,Body: %s,%s,%s".
                        format(fromPhoneNumber, incomingLongCode, msg))
                      smsService.handleIncoming(subscription.get, msg)
                      smsService.verify(Subscription(phoneNumber = formattedPhoneNumber, longCode = incomingLongCode))
                      response = ResourceBundles.Messages.Verify.getString(subscription.get.offerName)
                    }
                    case "no" | "stop" | "unsubscribe" => {
                      log.info("NO|STOP|UNSUBSCRIBE RESPONSE - From,To,Body: %s,%s,%s".
                        format(fromPhoneNumber, incomingLongCode, msg))
                      smsService.handleIncoming(subscription.get, msg)
                      smsService.unsubscribe(Subscription(phoneNumber = formattedPhoneNumber, longCode = incomingLongCode))
                      response = ResourceBundles.Messages.Unsubscribe.getString
                    }
                    case _ => {
                      log.info("INVALID SUBSCRIBE RESPONSE - From,To,Body: %s,%s,%s".
                        format(fromPhoneNumber, incomingLongCode, msg))
                      response = ResourceBundles.Messages.SubscribeInvalid.getString
                    }
                  }
                }
                case _ => {
                  log.info("Cannot match incoming response with any outgoing request - From,To,Body: %s,%s,%s".
                    format(fromPhoneNumber, incomingLongCode, msg))
                  response = ResourceBundles.Messages.ErrorInvalidParam.getString
                }
              }
            }

          }

          case (Some(_), None) => response = ResourceBundles.Messages.ErrorMissingParam.getString("Body")

          case (None, Some(_)) => response = ResourceBundles.Messages.ErrorMissingParam.getString("From")

          case _ => response = ResourceBundles.Messages.ErrorMissingParam.getString("From") +
            ResourceBundles.Messages.ErrorMissingParam.getString("Body")
        }

        val responseMsg = createMessage(response)

        render
          .header("Content-type", "application/xml")
          .header("Content-length", responseMsg.size.toString)
          .status(200)
          .body(responseMsg).toFuture
      }
      catch {
        case e: SecurityException =>
          log.warning(s"Params: ${request.params.mkString(",")}\nSignature: ${request.headerMap.get(Config.current.provider.signatureHeader)}", e)
          render.body(createMessage(ResourceBundles.Messages.ErrorServerError.getString)).status(403).toFuture
        case e: InvalidPhoneNumberException =>
          log.error(s"Params: ${request.params.mkString(",")}\n${e.getMessage}", e)
          render.body(createMessage(e.getMessage)).status(404).toFuture
        case e: Exception =>
          log.error(s"Params: ${request.params.mkString(",")}\n${e.getMessage}\n${e.getStackTraceString}", e)
          render.body(createMessage(ResourceBundles.Messages.ErrorServerError.getString)).status(500).toFuture
      }

  }

  post("/api/sms/callback/twilio/status") {
    request =>
      try {
        validateRequest(request, Config.current.provider.smsStatusCallback)

        val messageId = request.params.getOrElse("MessageSid", "")
        val messageStatus = request.params.getOrElse("MessageStatus", "")
        smsService.updateMessageStatusByMessageId(messageId, messageStatus)
        log.info("Timestamp: |%s|, MessageId: %s, MessageStatus: %s".format((new Date()).toString, messageId, messageStatus))
        render
          .header("Content-type", "application/xml")
          .status(200).body("done").toFuture
      }
      catch {
        case e: SecurityException =>
          log.warning(s"Params: ${request.params.mkString(",")}\nSignature: ${request.headerMap.get(Config.current.provider.signatureHeader)}", e)
          render.body(createMessage(ResourceBundles.Messages.ErrorServerError.getString)).status(403).toFuture
        case e: Exception =>
          log.error(s"Params: ${request.params.mkString(",")}\n${e.getMessage}\n${e.getStackTraceString}", e)
          render.body(createMessage(ResourceBundles.Messages.ErrorServerError.getString)).status(500).toFuture
      }

  }

  private def createMessage(message: String): String = {
    """<?xml version="1.0" encoding="UTF-8"?>
        <Response>
          <Message>%s</Message>
        </Response>
    """.format(StringEscapeUtils.escapeXml(message))
  }

  private def isConfirmMsg(smallMsg: String): Boolean = {
    smallMsg match {
      case "confirm" | "confirmed" => true
      case _ => false
    }
  }

  protected def validateRequest(request: Request, url:String) = {
    val signature = request.headers.get(Config.current.provider.signatureHeader)
    if (Config.isProduction || signature != null) smsService.validateRequest(signature, url,request.params)
  }

  get("/ping") {
    request => render.body("pong").toFuture
  }
}
