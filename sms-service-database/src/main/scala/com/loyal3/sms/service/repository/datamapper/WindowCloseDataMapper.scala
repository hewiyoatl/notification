package com.loyal3.sms.service.repository.datamapper

import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper
import org.skife.jdbi.v2.sqlobject.{Bind, GetGeneratedKeys, SqlUpdate}
import java.sql.Timestamp
import com.loyal3.sms.service.repository.recordmapper.WindowCloseRecordMapper

/**
 * Created with IntelliJ IDEA.
 * User: hewi
 * Date: 4/4/14
 * Time: 12:04 PM
 * To change this template use File | Settings | File Templates.
 */
@RegisterMapper(Array(classOf[WindowCloseRecordMapper]))
trait WindowCloseDataMapper {
  @SqlUpdate("INSERT INTO window_close (offer_id, price_end_date) VALUES (:offer_id, :price_end_date)")
  @GetGeneratedKeys
  def create(@Bind("offer_id") offerId: String,
             @Bind("price_end_date") priceEndDate: Timestamp): Long

  @SqlUpdate("update window_close set price_end_date = :price_end_date WHERE offer_id = :offer_id")
  def updateUpdatePriceEndDate(@Bind("offer_id") offerId: String,
                               @Bind("price_end_date") priceEndDate: Timestamp): Int

}
