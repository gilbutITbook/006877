import com.typesafe.sbt.SbtMultiJvm
import com.typesafe.sbt.SbtMultiJvm.MultiJvmKeys.MultiJvm

val akkaVersion = "2.5.0"

val project = Project(
  id = "channels",
  base = file("."),
  settings = Defaults.coreDefaultSettings ++ SbtMultiJvm.multiJvmSettings ++ Seq(
    name := "akka-sample-multi-node-scala",
    organization := "manning",
    version := "1.0",
    libraryDependencies ++= Seq(
    "com.typesafe.akka" %%  "akka-actor"              % akkaVersion,
    "com.typesafe.akka" %%  "akka-slf4j"              % akkaVersion,
    "com.typesafe.akka" %%  "akka-remote"             % akkaVersion,
    "com.typesafe.akka" %%  "akka-multi-node-testkit" % akkaVersion,
    "com.typesafe.akka" %%  "akka-contrib"            % akkaVersion,
    "com.typesafe.akka" %%  "akka-testkit"            % akkaVersion  % "test",
    "org.scalatest"     %%  "scalatest"               % "3.0.1"      % "test"
    ),
    // MultiJvm 테스트가 디폴트 테스트 컴파일에 의해 컴파일되도록 한다
    compile in MultiJvm := ((compile in MultiJvm) triggeredBy (compile in Test)).value,
    // 병렬 테스트를 비활성화 시킨다
    parallelExecution in Test := false,
    // MultiJvm 테스트가 디폴트 테스트 대상에 의해 실행되도록 한다
    // 그리고 일반적인 테스트의 결과와 multi-jvm의 테스트 결과를 함께 합친다
    executeTests in Test := {
      val testResults = (executeTests in Test).value
      val multiNodeResults = (executeTests in MultiJvm).value
      val overall =
          if (testResults.overall.id < multiNodeResults.overall.id)
            multiNodeResults.overall
          else
            testResults.overall
      Tests.Output(overall,
        testResults.events ++ multiNodeResults.events,
        testResults.summaries ++ multiNodeResults.summaries)
    }
  )
) configs (MultiJvm)
