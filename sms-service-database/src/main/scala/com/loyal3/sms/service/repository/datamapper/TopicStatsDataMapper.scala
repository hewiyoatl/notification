package com.loyal3.sms.service.repository.datamapper

import com.loyal3.sms.core.TopicStats
import com.loyal3.sms.service.repository.recordmapper.TopicStatsRecordMapper
import org.skife.jdbi.v2.sqlobject.{Bind, SqlQuery}
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper

@RegisterMapper(Array(classOf[TopicStatsRecordMapper]))
trait TopicStatsDataMapper {

  @SqlQuery("""
    SELECT :topic as topic, current_subscriptions, unsubscriptions, deleted_subscriptions, initial_subscriptions,
      subscribe_messages_queued, subscribe_messages_sent,
      confirm_messages_queued, confirm_messages_sent
    FROM
      (SELECT count(*) current_subscriptions
      FROM subscriptions
      WHERE state = 'SUBSCRIBED'
      AND topic = :topic) cs
    JOIN
      (SELECT count(*) unsubscriptions
      FROM subscriptions
      WHERE state = 'UNSUBSCRIBED'
      AND topic = :topic) us
    JOIN
      (SELECT count(*) deleted_subscriptions
      FROM subscriptions
      WHERE state = 'DELETED'
      AND topic = :topic) ds
    JOIN
      (SELECT count(*) initial_subscriptions
      FROM subscriptions
      WHERE topic = :topic) ts
    JOIN
      (SELECT count(*) subscribe_messages_sent
      FROM requests r
      JOIN subscriptions s
        ON s.id = r.subs_id
      WHERE r.message_status = 'sent'
        AND r.msg_type = 'SUBSCRIBE'
        AND s.topic = :topic) smc
    JOIN
      (SELECT count(*) subscribe_messages_queued
      FROM requests r
      JOIN subscriptions s
        ON s.id = r.subs_id
      WHERE r.msg_type = 'SUBSCRIBE'
        AND s.topic = :topic) sms
    JOIN
      (SELECT count(*) confirm_messages_sent
      FROM requests r
      JOIN subscriptions s
        ON s.id = r.subs_id
      WHERE r.message_status = 'sent'
        AND r.msg_type = 'CONFIRM'
        AND s.topic = :topic) cmc
    JOIN
      (SELECT count(*) confirm_messages_queued
      FROM requests r
      JOIN subscriptions s
        ON s.id = r.subs_id
      WHERE r.msg_type = 'CONFIRM'
        AND s.topic = :topic) cms """)
  def findByTopic(@Bind("topic") topic: String): TopicStats

}
