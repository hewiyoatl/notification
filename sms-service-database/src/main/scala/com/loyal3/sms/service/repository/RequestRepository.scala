package com.loyal3.sms.service.repository

import com.loyal3.sms.service.repository.datamapper.RequestDataMapper
import java.sql.SQLException
import com.loyal3.sms.core.Request

trait RequestRepository {

  def create(request: Request): Long

  def updateMessageStatus(messageStatus: String, messageId: String, subsId: String): Int

  def updateMessageStatusByMessageId(messageStatus: String, messageId: String): Int
}

class JdbiRequestRepository(mapper: RequestDataMapper) extends RequestRepository {

  @throws(classOf[SQLException])
  def create(request: Request): Long = {
    mapper.create(
      subsId = request.subsId.get,
      msgBody = request.msgBody.get,
      msgType = request.msgType.toString
    )
  }

  def updateMessageStatus(messageStatus: String, messageId: String, subsId: String): Int = {
    mapper.updateMessageStatus(messageStatus, messageId, subsId)
  }

  def updateMessageStatusByMessageId(messageStatus: String, messageId: String): Int = {
    mapper.updateMessageStatusByMessageId(messageStatus, messageId)
  }
}
