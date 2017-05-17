scalaVersion := "2.12.2"

scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked",
  "-Xfatal-warnings",
  "-Ywarn-dead-code",
  "-feature",
  "-language:_",
  "-Xlint:-unused,_",
  "-Ywarn-unused:imports"
)
