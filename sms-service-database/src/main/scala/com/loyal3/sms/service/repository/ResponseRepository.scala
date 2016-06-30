package com.loyal3.sms.service.repository

import com.loyal3.sms.service.repository.datamapper.ResponseDataMapper
import java.sql.Timestamp
import scala.collection.JavaConversions._
import com.loyal3.sms.core.IncomingSMS

trait ResponseRepository {

  def create(subsId: String, msgBody: String, createdAt: Option[Timestamp] = Some(new Timestamp(System.currentTimeMillis / 1000 * 1000))): Long

  def findById(id: Long): Option[IncomingSMS]

  def findAll: Seq[IncomingSMS]

  def findByUserId(userId: String): Seq[IncomingSMS]

  def findByTopic(topic: String, startDate: Long, endDate: Long,
                  startSeq: Long, endSeq: Long): Seq[IncomingSMS]

}

class JdbiResponseRepository(mapper: ResponseDataMapper) extends ResponseRepository {

  def create(subsId: String, msgBody: String, createdAt: Option[Timestamp]): Long = {
    mapper.create(subsId, createdAt.get, createdAt.get, msgBody)
  }

  def findById(id: Long): Option[IncomingSMS] = {
    Option(mapper.findById(id))
  }

  def findAll: Seq[IncomingSMS] = {

    val responses = mapper.findAll
    if (responses != null) asScalaBuffer(responses) else Seq()
  }

  def findByUserId(userId: String): Seq[IncomingSMS] = {
    val responses = mapper.findByUserId(userId)
    if (responses != null) asScalaBuffer(responses) else Seq()
  }

  def findByTopic(topic: String, startDate: Long, endDate: Long, startSeq: Long, endSeq: Long): Seq[IncomingSMS] = {
    val responses = mapper.findByTopic(topic, new Timestamp(startDate), new Timestamp(endDate), startSeq, endSeq)
    if (responses != null) asScalaBuffer(responses) else Seq()
  }
}
