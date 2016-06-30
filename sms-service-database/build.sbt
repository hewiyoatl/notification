name := "sms-service-database"

version := "0.1"

organization := "com.loyal3"

scalaVersion := "2.10.6"

{
  libraryDependencies ++= Seq(
    "com.zaxxer" % "HikariCP" % "2.4.5" withSources(),
    "mysql" % "mysql-connector-java" % "5.1.38",
    "org.jdbi" % "jdbi" % "2.73",
    "org.liquibase" % "liquibase-core" % "3.0.7"
  )
}

// append several options to the list of options passed to the Java compiler
javacOptions ++= Seq("-source", "1.6", "-target", "1.6")
