package com.loyal3.sms.service.repository

import java.sql.{Connection, DriverManager}
import com.mysql.jdbc.Driver
import liquibase.integration.commandline.Main

object Database {
  DriverManager.registerDriver(new Driver)

  def migrate(url: String, username: String, password: String) {
    val args = Array(
      "--driver", classOf[Driver].getName,
      "--changeLogFile", "db/db.changelog.xml",
      "--url", url + "?characterEncoding=utf8",
      "--username", username,
      "--password", password,
      "update"
    )
    Main.main(args)
  }

  def create(url: String, username: String, password: String, database: String) {
    val connection: Connection = DriverManager.getConnection(url, username, password)
    val statement = connection.createStatement()
    statement.execute("create database if not exists %s DEFAULT CHARACTER SET utf8" format database)
    statement.close()
    connection.close()
  }
}
