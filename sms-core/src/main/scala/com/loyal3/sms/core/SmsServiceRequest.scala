package com.loyal3.sms.core

trait SmsServiceRequest{

  def userId: String

  def phoneNumber: String

  def topic: String

  def longCode: String

  def offerName: String

  override def toString: String = "user id: %s, phone number: %s, topic: %s, longCode: %s".format(userId, phoneNumber, topic, longCode)
}

case class SmsServiceResponse(code: String,
                                causes: Option[Array[String]] = None,
                                subscriptionId: Option[String] = None)
