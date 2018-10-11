name := "argus"
organization := "com.talbs"
version := "0.1"
scalaVersion := "2.11.12"

val api = project.in(file("api"))
val client = project.in(file("client")).dependsOn(api)
val server = project.in(file("."))
  .enablePlugins(PlayScala,DockerPlugin)
  .dependsOn(api,client)
  .aggregate(api,client)
  .settings(libraryDependencies ++= Seq(
    //IOC
    guice,
    
    //service api
    "com.talbs" %% "argus-api" % "0.1",
    
    //client
    "com.talbs" %% "argus-client" % "0.1"
  ))
