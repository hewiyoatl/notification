package com.loyal3.sms.core

import java.sql.Timestamp
import java.util.UUID

case class IncomingSMSBuilder(private val id: Long = 0L,
                               private val subsId: String = null,
                               private val userId: String = null,
                               private val createdAt: Timestamp = null,
                               private val updatedAt: Timestamp = null,
                               private val phoneNumber: String = null,
                               private val topic: String = null,
                               private val msgBody: String = null) {

  def withRandomValues(): IncomingSMSBuilder = {
    val now: Timestamp = new Timestamp(System.currentTimeMillis / 1000 * 1000)
    this
      .withSubsId(UUID.randomUUID().toString)
      .withUserId(UUID.randomUUID().toString)
      .withCreatedAt(now)
      .withUpdatedAt(now)
      .withPhoneNumber("8005551212")
      .withMsgBody("some_message_body")
  }

  def withSubsId(subsId: String): IncomingSMSBuilder = {
    copy(subsId = subsId)
  }

  def withUserId(userId: String): IncomingSMSBuilder = {
    copy(userId = userId)
  }

  def withCreatedAt(createdAt: Timestamp): IncomingSMSBuilder = {
    copy(createdAt = createdAt)
  }

  def withUpdatedAt(updatedAt: Timestamp): IncomingSMSBuilder = {
    copy(updatedAt = updatedAt)
  }

  def withPhoneNumber(phoneNumber: String): IncomingSMSBuilder = {
    copy(phoneNumber = phoneNumber)
  }

  def withMsgBody(msgBody: String): IncomingSMSBuilder = {
    copy(msgBody = msgBody)
  }

  def withTopic(topic: String): IncomingSMSBuilder = {
    copy(topic = topic)
  }

  def build: IncomingSMS = {
    IncomingSMS(
      id = id,
      subsId = subsId,
      userId = userId,
      createdAt = createdAt,
      updatedAt = updatedAt,
      phoneNumber = phoneNumber,
      topic = topic,
      msgBody = msgBody
    )
  }
}
