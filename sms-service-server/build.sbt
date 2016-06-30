name := "sms-service-server"

version := "0.1"

organization := "com.loyal3"

scalaVersion := "2.10.6"

resolvers += "Loyal3 Artifact Repository" at "http://artifact.loyal3.net/artifactory/libs-release-local"

resolvers += "Twitter Repo" at "http://maven.twttr.com/"

{
  libraryDependencies ++= Seq(
    "com.loyal3.service.health" %% "service-health-finatra" % "1.48.bfecdba",
    "com.loyal3.service.status" %% "service-status-finatra" % "2.66.f896bd0"
  )
}

// append several options to the list of options passed to the Java compiler
javacOptions ++= Seq("-source", "1.6", "-target", "1.6")

assemblyJarName in assembly := "sms-service.jar"

test in assembly := {}

mainClass in assembly := Some("com.loyal3.sms.service.server.Main")

// Mark the build-resources directory as a resouce directory for the project
unmanagedResourceDirectories in Compile <+= baseDirectory(_ / "build-resources")
