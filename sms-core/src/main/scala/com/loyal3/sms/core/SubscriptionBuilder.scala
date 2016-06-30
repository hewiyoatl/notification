package com.loyal3.sms.core

import java.sql.Timestamp
import java.util.UUID
import scala.util.Random

case class SubscriptionBuilder(private val id: String = null,
                               private val userId: String = null,
                               private val phoneNumber: String = null,
                               private val createdAt: Timestamp = null,
                               private val updatedAt: Timestamp = null,
                               private val state: SubscriptionState = null,
                               private val topic: String = null,
                               private val longCode: String = null,
                               private val offerName: String = null
                               ) {
  def withRandomValues(): SubscriptionBuilder = {
    val now: Timestamp = new Timestamp(System.currentTimeMillis / 1000 * 1000)
    this
      .withId(UUID.randomUUID().toString)
      .withUserId(UUID.randomUUID().toString)
      .withPhoneNumber("8005551212")
      .withCreatedAt(now)
      .withUpdatedAt(now)
      .withState(SubscriptionState.SUBSCRIBED)
      .withTopic("some_topic")
      .withLongCode("4153453456")
      .withOfferName(Random.alphanumeric.take(20).mkString)
  }

  def withId(id: String): SubscriptionBuilder = {
    copy(id = id)
  }

  def withUserId(userId: String): SubscriptionBuilder = {
    copy(userId = userId)
  }

  def withPhoneNumber(phoneNumber: String): SubscriptionBuilder = {
    copy(phoneNumber = phoneNumber)
  }

  def withCreatedAt(createdAt: Timestamp): SubscriptionBuilder = {
    copy(createdAt = createdAt)
  }

  def withUpdatedAt(updatedAt: Timestamp): SubscriptionBuilder = {
    copy(updatedAt = updatedAt)
  }

  def withState(state: SubscriptionState): SubscriptionBuilder = {
    copy(state = state)
  }

  def withTopic(topic: String): SubscriptionBuilder = {
    copy(topic = topic)
  }
  def withLongCode(longCode: String): SubscriptionBuilder = {
    copy(longCode = longCode)
  }

  def withOfferName(offerName: String): SubscriptionBuilder = {
    copy(offerName = offerName)
  }

  def build: Subscription = {
    Subscription(
      id = id,
      userId = userId,
      phoneNumber = phoneNumber,
      createdAt = createdAt,
      updatedAt = updatedAt,
      state = state,
      topic = topic,
      longCode = longCode,
      offerName = offerName
    )
  }
}
