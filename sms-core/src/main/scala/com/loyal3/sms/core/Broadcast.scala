package com.loyal3.sms.core


case class Broadcast(userIds: List[String] = null,
                     topic: String = null,
                     msgBody: String = null,
                     pricingDate: Long = 0L,
                     msgType: OutgoingMessageType = OutgoingMessageType.CONFIRM) {

  override def toString: String = "offerId: %s, msgBody: %s, userIds: %s, pricingDate: %s".format(
      topic, msgBody, userIds, pricingDate)
}