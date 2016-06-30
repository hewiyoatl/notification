package com.loyal3.sms.service.repository

import com.loyal3.sms.config.{DatabaseConfig, Config}

object DatabaseManager {



  def main(args: Array[String]) {
    val db: DatabaseConfig = Config.current.database
    def url(schema: String = "mysql"): String = s"jdbc:mysql://${db.host}:${db.port}/$schema"
    Database.create(url(), db.username, db.password, db.database)
    Database.migrate(url(db.database), db.username, db.password)
  }
}
