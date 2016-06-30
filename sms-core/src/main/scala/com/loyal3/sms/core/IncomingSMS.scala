package com.loyal3.sms.core

import java.sql.Timestamp

case class IncomingSMS(id: Long = 0L,
                        subsId: String = null,
                        userId: String = null,
                        createdAt: Timestamp = null,
                        updatedAt: Timestamp = null,
                        phoneNumber: String = null,
                        topic: String = null,
                        longCode: String = null,
                        msgBody: String = null) extends IncomingSMSRequest {

  override def toString: String = "id: %s, subs id: %s, user id: %s, createdAt: %s, updatedAt: %s, phoneNumber: %s, topic: %s, message body: %s, long code: %s".format(
    id, subsId, userId, createdAt, updatedAt, phoneNumber, topic, msgBody, longCode)
}