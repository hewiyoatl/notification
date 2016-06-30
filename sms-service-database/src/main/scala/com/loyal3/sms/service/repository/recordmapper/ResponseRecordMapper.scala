package com.loyal3.sms.service.repository.recordmapper

import org.skife.jdbi.v2.tweak.ResultSetMapper
import java.sql.{Timestamp, ResultSet}
import org.skife.jdbi.v2.StatementContext
import com.loyal3.sms.core.IncomingSMS

class ResponseRecordMapper extends ResultSetMapper[IncomingSMS]{
  def map(index: Int, resultSet: ResultSet, context: StatementContext): IncomingSMS = {
    IncomingSMS(
      id = resultSet.getLong("id"),
      subsId = resultSet.getString("subs_id"),
      userId = resultSet.getString("user_id"),
      createdAt = resultSet.getTimestamp("created_at"),
      updatedAt = resultSet.getTimestamp("updated_at"),
      topic = resultSet.getString("topic"),
      msgBody = resultSet.getString("msg_body")
    )
  }
}