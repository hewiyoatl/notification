package com.loyal3.sms.service.repository.recordmapper

import java.sql.ResultSet
import org.skife.jdbi.v2.StatementContext
import com.loyal3.sms.core.WindowClose
import org.skife.jdbi.v2.tweak.ResultSetMapper

/**
 * Created with IntelliJ IDEA.
 * User: hewi
 * Date: 4/4/14
 * Time: 12:07 PM
 * To change this template use File | Settings | File Templates.
 */
class WindowCloseRecordMapper extends ResultSetMapper[WindowClose] {
  def map(index: Int, resultSet: ResultSet, context: StatementContext): WindowClose = {
    WindowClose(
      offerId = resultSet.getString("offer_id"),
      priceEndDate = resultSet.getTimestamp("price_end_date")
    )
  }

}
