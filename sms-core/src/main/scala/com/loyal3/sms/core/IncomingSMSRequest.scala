package com.loyal3.sms.core

trait IncomingSMSRequest{

  def userId: String

  def subsId: String

  def msgBody: String

  def topic: String

  def phoneNumber: String

  def longCode: String

  override def toString: String = "user id: %s, subs id: %s, message body: %s".format(userId, subsId, msgBody)
}

case class IncomingSMSResponse(code: String,
                              causes: Option[Array[String]] = None,
                              subscriptionId: Option[String] = None)

