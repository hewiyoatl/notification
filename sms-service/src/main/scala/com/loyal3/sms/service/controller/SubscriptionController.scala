package com.loyal3.sms.service.controller

import java.net.URLDecoder

import com.loyal3.sms.config.Config
import com.loyal3.sms.core.util.Json
import com.loyal3.sms.core.{Broadcast, SmsServiceResponse, Subscription}
import com.loyal3.sms.service.SmsService
import com.twitter.finatra.Controller
import org.apache.http.HttpStatus

import scala.util.control.NonFatal

class SubscriptionController(smsService: SmsService) extends Controller {
  post("/api/sms/subscriptions") {
    request =>
      try {
        val subscription: Subscription = Json.mapper
          .readValue(request.getContentString(), classOf[Subscription])

        log.info("POST /api/sms/subscriptions called with offerName,topic,userId: %s,%s,%s".
          format(subscription.offerName, subscription.topic, subscription.userId))

        val subscriptionResponse: SmsServiceResponse = smsService.subscribe(subscription)
        val status = subscriptionResponse.code match {
          case "ok" => HttpStatus.SC_OK
          case _ => HttpStatus.SC_BAD_REQUEST
        }

        render.body(Json.mapper.writeValueAsString(subscriptionResponse))
          .status(status)
          .toFuture
      } catch {
        case NonFatal(e) => {
          log.error(e, e.getMessage)
          render.body("Error").status(HttpStatus.SC_INTERNAL_SERVER_ERROR).toFuture
        }
      }
  }

  /**
    * Resubscribes an unsubscribed user
    */
  patch("/api/sms/subscriptions") {
    request =>
      try {
        val subscription: Subscription = Json.mapper
          .readValue(request.getContentString(), classOf[Subscription])

        log.info(s"PATCH /api/sms/subscriptions called with offerName, topic, userId: ${subscription.offerName}, ${subscription.topic}, ${subscription.userId}")

        val response: SmsServiceResponse = smsService.resubscribe(subscription)
        val status = response.code match {
          case "ok" => HttpStatus.SC_OK
          case _ => HttpStatus.SC_BAD_REQUEST
        }

        render.body(Json.mapper.writeValueAsString(response))
          .status(status)
          .toFuture
      } catch {
        case NonFatal(e) => {
          log.error(e, e.getMessage)
          render.body("Error").status(HttpStatus.SC_INTERNAL_SERVER_ERROR).toFuture
        }
      }
  }


  /**
    * Getting a subscription based on subscription id
    */
  get("/api/admin/sms/subscriptions/:id") {
    request =>
      try {
        val id = request.routeParams.get("id")
        val authKey: Option[String] = Option(request.headers().get(Config.current.security.headerName))
        log.info("/api/admin/sms/subscriptions/:id called with %s".format(id))

        if (validAuthKey(authKey)) {
          id match {
            case Some(id) => {
              val urlDecodedId = URLDecoder.decode(id, "UTF-8")
              val subscription = smsService.getSubscription(urlDecodedId)
              subscription match {
                case Some(s) => render.body(Json.mapper.writeValueAsString(s))
                  .status(HttpStatus.SC_OK)
                  .toFuture

                case None => render.body(Json.mapper.writeValueAsString("Not Found"))
                  .status(HttpStatus.SC_NOT_FOUND)
                  .toFuture
              }
            }

            //Shouldn't go here, but just in case
            case None => render.body(Json.mapper.writeValueAsString("Not Found")).status(HttpStatus.SC_NOT_FOUND).toFuture
          }
        } else {
          render.body(Json.mapper.writeValueAsString("Missing parameter.")).status(HttpStatus.SC_BAD_REQUEST).toFuture
        }
      }
      catch {
        case NonFatal(e) => {
          log.error(e, e.getMessage)
          render.body("Error").status(HttpStatus.SC_INTERNAL_SERVER_ERROR).toFuture
        }
      }
  }

  /**
    * Getting subscriptions based on user ID
    */
  get("/api/admin/sms/user/:userId/subscriptions") {
    request =>
      try {
        val userId: Option[String] = request.routeParams.get("userId")
        val authKey: Option[String] = Option(request.headers().get(Config.current.security.headerName))
        log.info("/api/admin/sms/user/:userId/subscriptions called with %s".format(userId))

        if (validAuthKey(authKey)) {
          userId match {
            case Some(u) => {
              val urlDecodedUserId = URLDecoder.decode(u, "UTF-8")
              val subscriptions = smsService.subscriptionsByUser(urlDecodedUserId)
              val status = if (subscriptions.length == 0) HttpStatus.SC_NOT_FOUND else HttpStatus.SC_OK
              render.body(Json.mapper.writeValueAsString(subscriptions)).status(status).toFuture
            }

            //Shouldn't go here, but just in case
            case None => render.body(Json.mapper.writeValueAsString("Not Found")).status(HttpStatus.SC_NOT_FOUND).toFuture
          }
        } else {
          render.body(Json.mapper.writeValueAsString("Missing parameter.")).status(HttpStatus.SC_BAD_REQUEST).toFuture
        }
      }
      catch {
        case NonFatal(e) => {
          log.error(e, e.getMessage)
          render.body("Error").status(HttpStatus.SC_INTERNAL_SERVER_ERROR).toFuture
        }
      }
  }

  /**
    * Getting subscriptions based on topic
    */
  get("/api/admin/sms/topic/:topic/subscriptions") {
    request =>
      try {
        val topic: Option[String] = request.routeParams.get("topic")
        val authKey: Option[String] = Option(request.headers().get(Config.current.security.headerName))
        val statusFilter: Option[String] = request.params.get("status")

        log.info("/api/admin/sms/subscriptions?topic=:topic called with %s,%s".format(topic, statusFilter))

        if (validAuthKey(authKey)) {
          topic match {
            case Some(t) => {
              val urlDecodedTopic = URLDecoder.decode(t, "UTF-8")

              val subscriptions: Seq[Subscription] = smsService.subscriptionsByTopic(urlDecodedTopic, statusFilter)
              val httpStatus = if (subscriptions.length == 0) HttpStatus.SC_NOT_FOUND else HttpStatus.SC_OK

              render.body(Json.mapper.writeValueAsString(subscriptions)).status(httpStatus).toFuture
            }

            //Shouldn't go here, but just in case
            case None => render.body(Json.mapper.writeValueAsString("Not Found")).status(HttpStatus.SC_NOT_FOUND).toFuture
          }
        } else {
          render.body(Json.mapper.writeValueAsString("Missing parameter.")).status(HttpStatus.SC_BAD_REQUEST).toFuture
        }
      } catch {
        case NonFatal(e) => {
          log.error(e, e.getMessage)
          render.body("Error").status(HttpStatus.SC_INTERNAL_SERVER_ERROR).toFuture
        }
      }
  }

  /**
    * Getting responses based on topic
    */
  get("/api/admin/sms/topic/:topic/responses") {
    request =>
      try {
        val topic: Option[String] = request.routeParams.get("topic")
        val startAt: Option[String] = request.params.get("start_at")
        val endAt: Option[String] = request.params.get("end_at")
        val startSeqAt: Option[String] = request.params.get("start_seq")
        val endSeqAt: Option[String] = request.params.get("end_seq")

        log.info("/api/admin/sms/topic/:topic/responses called with topic,startAt,endAt,startSeqAt,endSeqAt: %s,%s,%s,%s,%s".
          format(topic, startAt, endAt, startSeqAt, endSeqAt))

        topic match {
          case Some(t) => {
            val urlDecodedTopic = URLDecoder.decode(t, "UTF-8")

            def convertStringToLong(value: Option[String], defaultValue: Long): Long = {
              value match {
                case Some("") => {
                  defaultValue
                }
                case Some(s) => {
                  s.toLong
                }
                case None => {
                  defaultValue
                }
              }
            }

            val responses = smsService.responsesByTopic(
              urlDecodedTopic,
              convertStringToLong(startAt, 0L),
              convertStringToLong(endAt, Long.MaxValue),
              convertStringToLong(startSeqAt, 0L),
              convertStringToLong(endSeqAt, Long.MaxValue))
            val status = HttpStatus.SC_OK
            render.body(Json.mapper.writeValueAsString(responses)).status(status).toFuture
          }

          //Shouldn't go here, but just in case
          case None => render.body(Json.mapper.writeValueAsString("Not Found")).status(HttpStatus.SC_NOT_FOUND).toFuture
        }
      }
      catch {
        case NonFatal(e) => {
          log.error(e, e.getMessage)
          render.body("Error").status(HttpStatus.SC_INTERNAL_SERVER_ERROR).toFuture
        }
      }
  }

  /**
    * Release phone numbers in provider for the given topic
    */
  delete("/api/admin/sms/topic/:topic/phones") {
    request =>
      val releaseResult = request.routeParams.get("topic")
        .map(smsService.releaseProviderPhoneNumbers(_))
      render.status(
        releaseResult match  {
          case Some(true) => HttpStatus.SC_ACCEPTED
          case _ => HttpStatus.SC_BAD_REQUEST
        }).toFuture
  }

  /**
    * Getting subscriptions based on phone number
    */
  get("/api/admin/sms/phone/:phoneNumber/subscriptions") {
    request =>
      try {
        val phoneNumber: Option[String] = request.routeParams.get("phoneNumber")
        val authKey: Option[String] = Option(request.headers().get(Config.current.security.headerName))

        if (validAuthKey(authKey)) {
          phoneNumber match {
            case Some(phoneNum) => {
              val urlDecodedPhoneNum = URLDecoder.decode(phoneNum, "UTF-8")

              val subscriptions = smsService.subscriptionsByPhoneNumber(urlDecodedPhoneNum)
              val status = if (subscriptions.length == 0) HttpStatus.SC_NOT_FOUND else HttpStatus.SC_OK
              render.body(Json.mapper.writeValueAsString(subscriptions)).status(status).toFuture
            }

            //Shouldn't go here, but just in case
            case None => render.body(Json.mapper.writeValueAsString("Not Found")).status(HttpStatus.SC_NOT_FOUND).toFuture
          }
        } else {
          render.body(Json.mapper.writeValueAsString("Missing parameter.")).status(HttpStatus.SC_BAD_REQUEST).toFuture
        }
      }
      catch {
        case NonFatal(e) => {
          log.error(e, e.getMessage)
          render.body("Error").status(HttpStatus.SC_INTERNAL_SERVER_ERROR).toFuture
        }
      }
  }

  post("/api/admin/sms/broadcast") {
    request =>
      try {
        val broadcast: Broadcast = Json.mapper
          .readValue(request.getContentString(), classOf[Broadcast])
        log.info("/api/admin/sms/broadcast called with topic,msgType,pricingDate,msgBody: %s,%s,%s,%s".
          format(broadcast.topic, broadcast.msgType, broadcast.pricingDate, broadcast.msgBody))
        val subscriptionResponse: SmsServiceResponse = smsService.send(broadcast)
        val status = subscriptionResponse.code match {
          case "ok" => HttpStatus.SC_OK
          case _ => HttpStatus.SC_BAD_REQUEST
        }

        render.body(Json.mapper.writeValueAsString(subscriptionResponse))
          .status(status)
          .toFuture
      }
      catch {
        case NonFatal(e) => {
          log.error(e, e.getMessage)
          render.body("Error").status(HttpStatus.SC_INTERNAL_SERVER_ERROR).toFuture
        }
      }
  }

  get("/api/admin/sms/topic/:topic/stats") {
    request =>
      request.routeParams.get("topic")
        .map(smsService.getTopicStats(_))
        .map(render.json(_).status(HttpStatus.SC_OK).toFuture)
        .getOrElse(render.status(HttpStatus.SC_NOT_FOUND).toFuture)
  }

  get("/ping") {
    request => render.body("pong").toFuture
  }

  private def validAuthKey(authKey: Option[String]): Boolean = {
    authKey match {
      case Some(token) => (token == Config.current.security.authKey)
      case None => false
    }
  }
}
