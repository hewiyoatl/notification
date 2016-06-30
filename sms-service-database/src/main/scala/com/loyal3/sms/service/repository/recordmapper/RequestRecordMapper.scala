package com.loyal3.sms.service.repository.recordmapper

import org.skife.jdbi.v2.tweak.ResultSetMapper
import com.loyal3.sms.core.{Request, OutgoingMessageType}
import java.sql.ResultSet
import org.skife.jdbi.v2.StatementContext

class RequestRecordMapper extends ResultSetMapper[Request] {
  def map(index: Int, resultSet: ResultSet, context: StatementContext): Request = {
    Request(
      subsId = Option(resultSet.getString("subs_id")),
      msgBody = Option(resultSet.getString("msg_body")),
      msgType = OutgoingMessageType.valueOf(resultSet.getString("msg_type"))
    )
  }

}
