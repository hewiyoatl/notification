name := "test-tools"

version := "0.1"

organization := "com.loyal3"

scalaVersion := "2.10.6"

coverageHighlighting := false

{
    libraryDependencies ++= Seq(
        "log4j" % "log4j" % "1.2.17",
        "org.slf4j" % "slf4j-log4j12" % "1.7.16",
        "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.6.3"
    )
}

{
    libraryDependencies ++= Seq(
        "org.mockito" % "mockito-all" % "1.9.5",
        "org.specs2" %% "specs2" % "2.3.1"
    )
}

// append several options to the list of options passed to the Java compiler
javacOptions ++= Seq("-source", "1.6", "-target", "1.6")
