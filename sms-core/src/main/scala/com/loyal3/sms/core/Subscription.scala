package com.loyal3.sms.core

import java.sql.Timestamp

case class Subscription(id: String = null,
                        userId: String = null,
                        phoneNumber: String = null,
                        createdAt: Timestamp = null,
                        updatedAt: Timestamp = null,
                        state: SubscriptionState = SubscriptionState.SUBSCRIBED,
                        topic: String = null,
                        offerName: String = null,
                        longCode: String = null,
                        priceEndDate: Timestamp = null) extends SmsServiceRequest {

  override def toString: String = "id: %s, user id: %s, phone number: %s, state: %s, topic: %s, offerName: %s, createdAt: %s, updatedAt: %s, long code: %s, price end Date: %s".format(
    id, userId, phoneNumber, state, topic, offerName, createdAt, updatedAt, longCode, priceEndDate)
}