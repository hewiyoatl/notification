package com.loyal3.sms.core

import java.util.UUID

/**
 * Created with IntelliJ IDEA.
 * User: hewi
 * Date: 4/7/14
 * Time: 12:09 PM
 * To change this template use File | Settings | File Templates.
 */
case class RequestBuilder(private val subsId:        Option[String]      = None,
                          private val msgBody:       Option[String]      = None,
                          private val msgType:       OutgoingMessageType = OutgoingMessageType.CONFIRM,
                          private val longCode:      Option[String]      = None,
                          private val messageId:     Option[String]      = None,
                          private val messageStatus: Option[String]      = None) {

  def withRandomValues(): RequestBuilder = {
    this
      .withSubsId(UUID.randomUUID().toString)
      .withLongCode(UUID.randomUUID().toString)
      .withMessageId(UUID.randomUUID().toString)
      .withMessageStatus("queued")
      .withMsgBody("This is the message body for testing purposes")
      .withMsgType(OutgoingMessageType.SUBSCRIBE)
  }

  def withSubsId(subsId: String): RequestBuilder = {
    copy(subsId = Option(subsId))
  }

  def withMsgBody(msgBody: String): RequestBuilder = {
    copy(msgBody = Option(msgBody))
  }

  def withMsgType(msgType: OutgoingMessageType): RequestBuilder = {
    copy(msgType = msgType)
  }

  def withLongCode(longCode: String): RequestBuilder = {
    copy(longCode = Option(longCode))
  }

  def withMessageId(messageId: String): RequestBuilder = {
    copy(messageId = Option(messageId))
  }

  def withMessageStatus(messageStatus: String): RequestBuilder = {
    copy(messageStatus = Option(messageStatus))
  }

  def build: Request = {
    Request(
      subsId = subsId,
      msgBody = msgBody,
      msgType = msgType,
      longCode = longCode,
      messageId = messageId,
      messageStatus = messageStatus
    )
  }

}
