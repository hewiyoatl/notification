package com.loyal3.sms.service.repository.datamapper

import com.loyal3.sms.service.repository.recordmapper.SubscriptionRecordMapper
import org.skife.jdbi.v2.sqlobject.{SqlQuery, SqlUpdate, Bind}
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper
import com.loyal3.sms.core.Subscription
import java.sql.Timestamp
import java.util

@RegisterMapper(Array(classOf[SubscriptionRecordMapper]))
trait SubscriptionDataMapper {
  @SqlUpdate("INSERT INTO subscriptions (id, user_id, phone_number, phone_digest, created_at, updated_at, state, topic, long_code, offer_name) VALUES (:id, :user_id, :phone_number, :phone_digest, :created_at, :updated_at, :state, :topic, :long_code, :offer_name)")
  def create(@Bind("id") id: String,
             @Bind("user_id") userId: String,
             @Bind("phone_number") phoneNumber: String,
             @Bind("phone_digest") phoneDigest: String,
             @Bind("created_at") createdAt: Timestamp,
             @Bind("updated_at") updatedAt: Timestamp,
             @Bind("state") state: String,
             @Bind("topic") topic: String,
             @Bind("long_code") longCode: String,
             @Bind("offer_name") offerName: String)

  @SqlQuery("SELECT id, user_id, phone_number, created_at, updated_at, state, topic, long_code, offer_name FROM subscriptions WHERE id = :id")
  def findById(@Bind("id") id: String): Subscription

  @SqlQuery("SELECT id, user_id, phone_number, created_at, updated_at, state, topic, long_code, offer_name FROM subscriptions")
  def findAll: util.List[Subscription]

  @SqlQuery("SELECT id, user_id, phone_number, created_at, updated_at, state, topic, long_code, offer_name FROM subscriptions WHERE user_id = :user_id")
  def findByUserId(@Bind("user_id") userId: String): util.List[Subscription]

  @SqlQuery("SELECT id, user_id, phone_number, created_at, updated_at, state, topic, long_code, offer_name FROM subscriptions WHERE topic = :topic")
  def findByTopic(@Bind("topic") topic: String): util.List[Subscription]

  @SqlQuery("SELECT id, user_id, phone_number, created_at, updated_at, state, topic, long_code, offer_name FROM subscriptions WHERE topic = :topic AND state = :statusFilter")
  def findByTopicAndStatus(@Bind("topic") topic: String, @Bind("statusFilter") statusFilter: String): util.List[Subscription]

  @SqlQuery("SELECT id, user_id, phone_number, created_at, updated_at, state, topic, long_code, offer_name FROM subscriptions WHERE phone_digest = :phone_digest ORDER BY created_at DESC")
  def findByPhoneNumber(@Bind("phone_digest") phoneDigest: String): util.List[Subscription]

  @SqlUpdate("update subscriptions set state = :state, updated_at = now() WHERE phone_digest = :phone_digest")
  def updateStateByPhoneNumber(@Bind("phone_digest") phoneDigest: String, @Bind("state") state: String): Int

  @SqlUpdate("update subscriptions set state = :state, updated_at = now() WHERE long_code = :long_code AND phone_digest = :phone_digest")
  def updateStateByLongCode(@Bind("long_code") longCode: String, @Bind("phone_digest") phoneDigest: String, @Bind("state") state: String): Int

  @SqlQuery("SELECT id, user_id, phone_number, created_at, updated_at, state, topic, long_code, offer_name FROM subscriptions WHERE user_id = :user_id AND topic = :topic")
  def findByUserIdAndTopic(@Bind("user_id") userId: String, @Bind("topic") topic: String): util.List[Subscription]

  @SqlQuery("SELECT id, user_id, phone_number, created_at, updated_at, state, topic, long_code, offer_name FROM subscriptions WHERE user_id = :user_id AND topic = :topic AND phone_digest = :phone_digest")
  def findByUserIdTopicAndPhoneNumber(@Bind("user_id") userId: String, @Bind("topic") topic: String, @Bind("phone_digest") phoneNumber: String): util.List[Subscription]

  @SqlQuery("SELECT id, user_id, phone_number, created_at, updated_at, state, topic, long_code, offer_name FROM subscriptions WHERE user_id = :user_id AND topic = :topic AND state = :statusFilter")
  def findByUserIdAndTopicStatus(@Bind("user_id") userId: String, @Bind("topic") topic: String, @Bind("statusFilter") statusFilter: String): util.List[Subscription]

  @SqlQuery("SELECT s.id, s.user_id, s.phone_number, s.created_at, s.updated_at, s.state, s.topic, s.long_code, w.price_end_date, s.offer_name FROM subscriptions s LEFT JOIN window_close w ON w.offer_id = s.topic WHERE long_code = :long_code AND phone_digest = :phone_digest ORDER BY created_at DESC")
  def findByLongCodePhoneNumber(@Bind("long_code") longCode: String, @Bind("phone_digest") phoneNumber: String): util.List[Subscription]

  @SqlUpdate("update subscriptions set long_code = :long_code WHERE id = :id")
  def updateLongCodeBySubsId(@Bind("id") subsId: String, @Bind("long_code") longCode: String): Int

  @SqlUpdate("update subscriptions set state = :state, long_code = :long_code WHERE id = :id")
  def updateStateLongCodeBySubsId(@Bind("id") subsId: String, @Bind("state") state: String, @Bind("long_code") longCode: String): Int

  @SqlQuery("SELECT r.msg_type FROM subscriptions s INNER JOIN requests r ON s.id = r.subs_id WHERE s.long_code = :long_code ORDER BY r.id DESC LIMIT 1")
  def findCurrentMessageTypeByLongCode(@Bind("long_code") longCode: String): String

  @SqlQuery("SELECT COUNT(1) FROM subscriptions WHERE user_id = :user_id AND topic = :topic AND phone_digest = :phone_digest")
  def duplicateSubscription(@Bind("user_id") userId: String, @Bind("topic") topic: String, @Bind("phone_digest") phoneDigest: String): Int

  @SqlQuery("SELECT 1 FROM subscriptions LIMIT 1")
  def healthCheck: Int
}

