package com.loyal3.sms.service.provider

import scala.concurrent._
import com.twilio.sdk.resource.instance.Sms
import org.apache.commons.lang.builder.ToStringBuilder
import com.loyal3.sms.core.Subscription

trait SmsProvider {
  def send(sendRequest: SendRequest, callback: (TwilioSendResponse) => Unit): Future[SendResponse]
  def getPhoneNumber(subscription: Subscription): Option[String]
  def releasePhoneNumber(phoneNumber: String): Boolean
  def validateRequest(signature: String, url: String, params: Map[String,String])
  def healthCheck: Boolean
  def healthCheckTwilioSMS: Boolean
}

case class SendRequest(subsId: String, fromPhone: String, phoneNumber: String, message: String)

trait SendResponse{
  def status: String
  def code: String

  override def toString: String = "status: %s, code: %s".format(status, code)
}

case class SimpleSendResponse(status: String, code: String) extends SendResponse

case class TwilioSendResponse(subscriptionId: String, code: String, sms: Sms) extends SendResponse{
  def status: String = sms.getStatus

  override def toString: String = super.toString + ToStringBuilder.reflectionToString(sms)
}