package com.loyal3.sms.core

case class TopicStats(
                     topic: String,
                     currentSubscriptions: Int,
                     unsubscriptions: Int,
                     deletedSubscriptions: Int,
                     initialSubscriptions: Int,
                     subscribeMessagesSent: Int,
                     subscribeMessagesQueued: Int,
                     confirmMessagesSent: Int,
                     confirmMessagesQueued: Int)