package com.loyal3.sms.service.repository.datamapper

import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper
import org.skife.jdbi.v2.sqlobject.{Bind, GetGeneratedKeys, SqlUpdate}
import com.loyal3.sms.service.repository.recordmapper.RequestRecordMapper

@RegisterMapper(Array(classOf[RequestRecordMapper]))
trait RequestDataMapper {
  @SqlUpdate("INSERT INTO requests (subs_id, msg_body, msg_type) VALUES (:subs_id, :msg_body, :msg_type)")
  @GetGeneratedKeys
  def create(@Bind("subs_id") subsId: String,
             @Bind("msg_body") msgBody: String,
             @Bind("msg_type") msgType: String): Long

  @SqlUpdate("update requests set message_status = :message_status, message_id = :message_id WHERE subs_id = :subs_id ORDER BY id DESC LIMIT 1")
  def updateMessageStatus(@Bind("message_status") messageStatus: String, @Bind("message_id") messageId: String, @Bind("subs_id") subsId: String): Int

  @SqlUpdate("update requests set message_status = :message_status WHERE message_id = :message_id")
  def updateMessageStatusByMessageId(@Bind("message_status") messageStatus: String, @Bind("message_id") messageId: String): Int
}
