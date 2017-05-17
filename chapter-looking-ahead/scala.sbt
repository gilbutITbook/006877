scalaVersion := "2.12.2"

scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked",
  // 만약 deprecated된 DSL을 사용해 보고 싶다면 아래 -Xfatal-warnings 부분을 없애야 한다.
  "-Xfatal-warnings",
  "-Xlint:-unused,_",
  "-Ywarn-unused:imports",
  "-Ywarn-dead-code",
  "-feature",
  "-language:_"
)
