name := "sms-service"

version := "0.1"

organization := "com.loyal3"

scalaVersion := "2.10.6"

resolvers += "Loyal3 Artifact Repository" at "http://artifact.loyal3.net/artifactory/libs-release-local"

resolvers += "Twitter Repo" at "http://maven.twttr.com/"

resolvers += Classpaths.sbtPluginReleases

// append several options to the list of options passed to the Java compiler
javacOptions ++= Seq("-source", "1.6", "-target", "1.6")
