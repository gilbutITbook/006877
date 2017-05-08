# 의존관계 변경

1. `build.sbt` : `akka` 버전은 "2.5.0"으로, `scalatest`는 "3.0.1"로 변경, `sbt` 버전 변경에 따라 다음을 변경
 - 9라인 `Defaults.coreDefaultSettings`으로 변경
 - 23라인 `compile in MultiJvm := ((compile in MultiJvm) triggeredBy (compile in Test)).value`로 변경
 - 29라인~39라인 mapping부분을 현재 형태로 변경
 ```
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

 ```
2. `scala.sbt` : 스칼라 버전을 2.12.2로 변경, `scalacOptions`에서 `"-Xfatal-warnings",` 제거 (ReliableProxy가 2.5부터는 deprecated라서 컴파일이 안되는 문제 해결)

3. `project\build.properties`: 추가
    sbt.version=0.13.15

4. `project\prugins.sbt`: `sbt-multi-jvm` 버전을 "0.3.11"로 변경