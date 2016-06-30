package com.loyal3.sms.service.provider.twilio

import scala.collection.mutable.{Map => MMap}

/**
 * Created with IntelliJ IDEA.
 * User: hewi
 * Date: 3/26/14
 * Time: 1:50 PM
 * To change this template use File | Settings | File Templates.
 */
object LongCodeMapper {
  private val map = MMap.empty[String, LongCode]

  def addOfferId(offerId: String, longCode: String): Unit = {
    map += (offerId -> LongCode(longCode = longCode, 1))
  }

  def existingLongCode(offerId: String): Option[LongCode] =
    if (map.contains(offerId)) {
      map.get(offerId).get.count += 1
      map.get(offerId)
    }
    else None
}

case class LongCode(longCode: String,
                    var count: Int) {
}
