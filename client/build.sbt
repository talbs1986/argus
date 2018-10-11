name := "argus-client"
organization := "com.talbs"
version := "0.1"
scalaVersion := "2.11.12"

libraryDependencies ++= Seq(
  //http client
  ws,

  //service api
  "com.talbs" %% "argus-api" % "0.1"
)