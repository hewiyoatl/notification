package com.loyal3.sms.core

import java.util.UUID

case class BroadcastBuilder(private val userIds: List[String] = null,
                            private val topic: String = null,
                            private val msgBody: String = null) {

  def withRandomValues(): BroadcastBuilder = {
    this
      .withMsgBody(UUID.randomUUID().toString)
      .withTopic(UUID.randomUUID().toString)
      .withUserIds(List("testUser1", "testUser2"))
  }

  def withUserIds(userIds: List[String]): BroadcastBuilder = {
    copy(userIds = userIds)
  }

  def withTopic(topic: String): BroadcastBuilder = {
    copy(topic = topic)
  }

  def withMsgBody(msgBody: String): BroadcastBuilder = {
    copy(msgBody = msgBody)
  }

  def build: Broadcast = {
    Broadcast(
      userIds = userIds,
      topic = topic,
      msgBody = msgBody
    )
  }
}
