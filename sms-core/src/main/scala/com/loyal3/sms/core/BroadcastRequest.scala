package com.loyal3.sms.core

trait BroadcastRequest {

  def userIds: List[String]

  def topic: String

  def msgBody: String

  override def toString: String = "user ids: %s, topic: %s, message body: %s".format(userIds, topic, msgBody)
}
