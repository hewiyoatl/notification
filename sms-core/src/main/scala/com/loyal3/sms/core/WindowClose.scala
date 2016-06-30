package com.loyal3.sms.core

import java.sql.Timestamp

/**
 * Created with IntelliJ IDEA.
 * User: hewi
 * Date: 4/4/14
 * Time: 12:09 PM
 * To change this template use File | Settings | File Templates.
 */
case class WindowClose(offerId: String = null,
                       priceEndDate: Timestamp = null) {

  override def toString: String = "offer id: %s, price end date: %s".format(offerId, priceEndDate)

}
