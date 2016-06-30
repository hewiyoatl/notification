package com.loyal3.sms.service.repository

import com.loyal3.sms.core.WindowClose
import java.sql.SQLException
import com.loyal3.sms.service.repository.datamapper.WindowCloseDataMapper

/**
 * Created with IntelliJ IDEA.
 * User: hewi
 * Date: 4/4/14
 * Time: 1:14 PM
 * To change this template use File | Settings | File Templates.
 */
trait WindowCloseRepository {

  def create(request: WindowClose): Long

  def updatePriceEndDate(request: WindowClose): Int
}

class JdbiWindowCloseRepository(mapper: WindowCloseDataMapper) extends WindowCloseRepository {
  @throws(classOf[SQLException])
  def create(request: WindowClose): Long = {
    mapper.create(
      offerId = request.offerId,
      priceEndDate = request.priceEndDate
    )
  }

  def updatePriceEndDate(windowClose: WindowClose): Int = {
    mapper.updateUpdatePriceEndDate(offerId = windowClose.offerId, priceEndDate = windowClose.priceEndDate)
  }
}
