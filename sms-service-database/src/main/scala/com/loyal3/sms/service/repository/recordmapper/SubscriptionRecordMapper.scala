package com.loyal3.sms.service.repository.recordmapper

import org.skife.jdbi.v2.tweak.ResultSetMapper
import java.sql.ResultSet
import org.skife.jdbi.v2.StatementContext
import com.loyal3.sms.core.{SubscriptionState, Subscription}
import scala.util.Try

class SubscriptionRecordMapper extends ResultSetMapper[Subscription]{
  def map(index: Int, resultSet: ResultSet, context: StatementContext): Subscription = {
    Subscription(
      id = resultSet.getString("id"),
      userId = resultSet.getString("user_id"),
      phoneNumber = resultSet.getString("phone_number"),
      createdAt = resultSet.getTimestamp("created_at"),
      updatedAt = resultSet.getTimestamp("updated_at"),
      state = SubscriptionState.valueOf(resultSet.getString("state")),
      topic = resultSet.getString("topic"),
      offerName = resultSet.getString("offer_name"),
      longCode = resultSet.getString("long_code"),
      priceEndDate = Try(resultSet.getTimestamp("price_end_date")).getOrElse(null)
    )
  }
}

