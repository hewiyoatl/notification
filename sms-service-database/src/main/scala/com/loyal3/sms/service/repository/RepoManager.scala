package com.loyal3.sms.service.repository

import com.loyal3.service.health._
import com.loyal3.sms.core.util.healthcheck.HealthCheckUtils
import com.twitter.util.Future
import org.skife.jdbi.v2.tweak.ConnectionFactory
import com.loyal3.sms.config.{Config, DatabaseConfig}
import org.skife.jdbi.v2.DBI
import com.loyal3.sms.service.repository.datamapper._
import com.zaxxer.hikari.HikariDataSource

class RepoManager {

  lazy val hikariCp = {
    val config: DatabaseConfig = Config.current.database
    val ds = new HikariDataSource()
    ds.setDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource")
    ds.setMaximumPoolSize(config.maxConnections)
    ds.addDataSourceProperty("serverName", config.host)
    ds.addDataSourceProperty("databaseName", config.database)
    ds.addDataSourceProperty("portNumber", config.port.toString)
    ds.addDataSourceProperty("user", config.username)
    ds.addDataSourceProperty("password", config.password)
    ds
  }
  lazy val connectionFactory: ConnectionFactory = {
    new ConnectionFactory {
      def openConnection() = hikariCp.getConnection
    }
  }

  val dbi: DBI = new DBI(connectionFactory)
  val subscriptionRepository: SubscriptionRepository = new JdbiSubscriptionRepository(dbi.onDemand(classOf[SubscriptionDataMapper]))
  val responseRepository: ResponseRepository = new JdbiResponseRepository(dbi.onDemand(classOf[ResponseDataMapper]))
  val requestRepository: RequestRepository = new JdbiRequestRepository(dbi.onDemand(classOf[RequestDataMapper]))
  val topicStatsRepository: TopicStatsRepository = new JdbiTopicStatsRepository(dbi.onDemand(classOf[TopicStatsDataMapper]))
  val windowCloseRepository: WindowCloseRepository = new JdbiWindowCloseRepository(dbi.onDemand(classOf[WindowCloseDataMapper]))

  val healthCheck: HealthCheckable = new HealthCheckable {
    private[service] val healthUtils: HealthCheckUtils = HealthCheckUtils
    override val name: String = "Subscription Repository Pool Check"
    override val description: String = "Ensures database pool is in good shape."
    override def checkHealth(): Future[HealthCheckResult] = {
      healthUtils.pool {
        healthUtils.tryHealthCheck(healthCheckPool)(_.getMessage)
      }
    }
  }

  private def healthCheckPool: HealthCheckResult = {
    if (hikariCp == null || hikariCp.isClosed)
      Warning("Datasource is closed")
    else
      Healthy()
  }
}

object RepoManager extends RepoManager
