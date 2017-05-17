# 의존관계 변경

1. `build.sbt` : 버전변경
  - `akka` 버전은 "2.5.0"으로
  - `scalatest`는 "3.0.1"로
  - `commons-io`는 "2.5"로
  - `akka-typed-experimental`을 `akka-typed`로

2. `scala.sbt` : 스칼라 버전을 2.12.2로 변경,   "-Xlint:-unused,_", "-Ywarn-unused:imports" 추가, "-Ywarn-unused" 제거

3. `project/build.properties`: `sbt.version=0.13.15`로 변경