package com.loyal3.sms.service.repository.recordmapper

import java.sql.ResultSet

import com.loyal3.sms.core.TopicStats
import org.skife.jdbi.v2.StatementContext
import org.skife.jdbi.v2.tweak.ResultSetMapper

class TopicStatsRecordMapper extends ResultSetMapper[TopicStats] {
  def map(index: Int, resultSet: ResultSet, context: StatementContext): TopicStats =
    TopicStats(
      topic = resultSet.getString("topic"),
      currentSubscriptions = resultSet.getInt("current_subscriptions"),
      unsubscriptions = resultSet.getInt("unsubscriptions"),
      deletedSubscriptions = resultSet.getInt("deleted_subscriptions"),
      initialSubscriptions = resultSet.getInt("initial_subscriptions"),
      subscribeMessagesSent = resultSet.getInt("subscribe_messages_sent"),
      subscribeMessagesQueued = resultSet.getInt("subscribe_messages_queued"),
      confirmMessagesSent = resultSet.getInt("confirm_messages_sent"),
      confirmMessagesQueued = resultSet.getInt("confirm_messages_queued")
    )
}
