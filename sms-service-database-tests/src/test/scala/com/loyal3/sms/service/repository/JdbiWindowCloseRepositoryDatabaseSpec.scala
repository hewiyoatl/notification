package com.loyal3.sms.service.repository

import com.loyal3.sms.test.support.scopes.SelfAwareSpecification
import org.skife.jdbi.v2.DBI
import com.loyal3.sms.service.repository.datamapper.WindowCloseDataMapper
import com.loyal3.sms.core.WindowCloseBuilder
import java.util.UUID

/**
 * Created with IntelliJ IDEA.
 * User: hewi
 * Date: 4/7/14
 * Time: 12:05 PM
 * To change this template use File | Settings | File Templates.
 */
class JdbiWindowCloseRepositoryDatabaseSpec extends SelfAwareSpecification {
  sequential

  val dbi: DBI = RepoManager.dbi
  val mapperWindowClose: WindowCloseDataMapper = dbi.onDemand(classOf[WindowCloseDataMapper])
  val windowCloseRepository = new JdbiWindowCloseRepository(mapperWindowClose)

  def cleanAll: Unit = {
    val handle = dbi.open()
    handle.execute("delete from window_close")
    handle.close
  }

  "#create" should {
    "create a window close record" in {
      cleanAll
      // Given
      val windowClose = WindowCloseBuilder().withRandomValues().build

      // When
      val id = windowCloseRepository.create(windowClose)

      // Then
      id mustNotEqual ""
    }
  }

  "#updatePriceEndDate" should {
    "update record created via #create" in {
      cleanAll

      val offerId = UUID.randomUUID.toString

      // Given
      val windowClose = WindowCloseBuilder().withOfferId(offerId).withPriceEndDate(null).build
      val windowCloseUpdate = WindowCloseBuilder().withRandomValues().withOfferId(offerId).build


      // When
      windowCloseRepository.create(windowClose)
      val updatedId = windowCloseRepository.updatePriceEndDate(windowCloseUpdate)

      // Then
      updatedId mustEqual 1
    }
  }


}
