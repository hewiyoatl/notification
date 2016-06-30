name := "sms-service-providers"

version := "0.1"

organization := "com.loyal3"

scalaVersion := "2.10.6"

resolvers += Classpaths.sbtPluginReleases

{
    libraryDependencies ++= Seq(

        "com.twilio.sdk" % "twilio-java-sdk" % "6.3.0" % "compile" withSources()
    )
}

// append several options to the list of options passed to the Java compiler
javacOptions ++= Seq("-source", "1.6", "-target", "1.6")
