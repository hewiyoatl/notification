package com.loyal3.sms.core

case class Request(subsId:        Option[String]      = None,
                   msgBody:       Option[String]      = None,
                   msgType:       OutgoingMessageType = OutgoingMessageType.CONFIRM,
                   longCode:      Option[String]      = None,
                   messageId:     Option[String]      = None,
                   messageStatus: Option[String]      = None) {
  override def toString: String = "subs id: %s, msg body: %s, msg type: %s".format(subsId, msgType)
}
