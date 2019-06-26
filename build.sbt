name := """log-analyze"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.0"

libraryDependencies ++= Seq(
    ws,
    guice,
    "org.webjars" % "swagger-ui" % "3.22.2",
    "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.3" % Test
)


scalacOptions ++= Seq(
    "-feature",
    "-deprecation",
    "-Xfatal-warnings"
)
