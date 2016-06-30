package com.loyal3.sms.service.repository.datamapper

import com.loyal3.sms.service.repository.recordmapper.ResponseRecordMapper
import org.skife.jdbi.v2.sqlobject.{GetGeneratedKeys, SqlQuery, SqlUpdate, Bind}
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper
import java.sql.Timestamp
import com.loyal3.sms.core.IncomingSMS
import java.util

@RegisterMapper(Array(classOf[ResponseRecordMapper]))
trait ResponseDataMapper {

  @SqlUpdate("INSERT INTO responses (subs_id, created_at, updated_at, msg_body) VALUES (:subs_id, :created_at, :updated_at, :msg_body)")
  @GetGeneratedKeys
  def create(@Bind("subs_id") subsId: String,
             @Bind("created_at") createdAt: Timestamp,
             @Bind("updated_at") updatedAt: Timestamp,
             @Bind("msg_body") msgBody: String): Long

  @SqlQuery("SELECT r.id, r.subs_id, s.user_id, r.created_at, r.updated_at, s.topic, r.msg_body FROM responses r join subscriptions s on r.subs_id = s.id WHERE r.id = :id")
  def findById(@Bind("id") id: Long): IncomingSMS

  @SqlQuery("SELECT r.id, r.subs_id, s.user_id, r.created_at, r.updated_at, s.topic, r.msg_body FROM responses r join subscriptions s on r.subs_id = s.id ORDER BY r.id ASC")
  def findAll: util.List[IncomingSMS]

  @SqlQuery("SELECT r.id, r.subs_id, s.user_id, r.created_at, r.updated_at, s.topic, r.msg_body FROM responses r join subscriptions s on r.subs_id = s.id WHERE s.user_id = :user_id ORDER BY r.id ASC")
  def findByUserId(@Bind("user_id") userId: String): util.List[IncomingSMS]

  @SqlQuery("SELECT r.id, r.subs_id, s.user_id, r.created_at, r.updated_at, s.topic, r.msg_body FROM responses r join subscriptions s on r.subs_id = s.id WHERE s.topic = :topic and r.created_at >= :start_date and r.created_at <= :end_date and r.id >= :start_seq and r.id <= :end_seq ORDER BY r.id ASC")
  def findByTopic(@Bind("topic") topic: String,
                  @Bind("start_date") startDate: Timestamp,
                  @Bind("end_date") endDate: Timestamp,
                  @Bind("start_seq") startSeqId: Long,
                  @Bind("end_seq") endSeqId: Long): util.List[IncomingSMS]


}