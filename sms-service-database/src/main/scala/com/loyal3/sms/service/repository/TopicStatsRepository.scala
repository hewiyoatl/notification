package com.loyal3.sms.service.repository

import com.loyal3.sms.core.TopicStats
import com.loyal3.sms.service.repository.datamapper.TopicStatsDataMapper

trait TopicStatsRepository {
  def findByTopic(topic: String): TopicStats
}
class JdbiTopicStatsRepository (mapper: TopicStatsDataMapper) extends TopicStatsRepository {
  def findByTopic(topic: String): TopicStats = mapper.findByTopic(topic)
}
