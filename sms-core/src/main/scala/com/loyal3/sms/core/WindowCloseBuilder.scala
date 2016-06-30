package com.loyal3.sms.core

import java.sql.Timestamp
import java.util.{Calendar, UUID}

/**
 * Created with IntelliJ IDEA.
 * User: hewi
 * Date: 4/7/14
 * Time: 12:09 PM
 * To change this template use File | Settings | File Templates.
 */
case class WindowCloseBuilder(private val offerId: String = null,
                              private val priceEndDate: Timestamp = null
                               ) {

  def withRandomValues(): WindowCloseBuilder = {
    val cal = Calendar.getInstance()
    cal.add(Calendar.DAY_OF_MONTH, 30)
    val now: Timestamp = new Timestamp(cal.getTimeInMillis)
    this
      .withOfferId(UUID.randomUUID().toString)
      .withPriceEndDate(now)
  }

  def withOfferId(offerId: String): WindowCloseBuilder = {
    copy(offerId = offerId)
  }

  def withPriceEndDate(priceEndDate: Timestamp): WindowCloseBuilder = {
    copy(priceEndDate = priceEndDate)
  }

  def build: WindowClose = {
    WindowClose(
      offerId = offerId,
      priceEndDate = priceEndDate
    )
  }

}
