# 의존관계 변경

1. `build.sbt` : `akka` 버전은 "2.5.0"으로, `scalatest`는 "3.0.1"로 번경, `nscala-time`은 2.16.0로 변경

2. `scala.sbt` : 스칼라 버전을 2.12.2로 변경

3. `project\build.properties`: 파일 추가. 내용은 다음과 같음

    sbt.version=0.13.15

4. `TicketInfoService.scala`에서:
    1. `val ticketInfos` 타입 변경: `Seq`대신 `List`를 직접 사용(`collection.Seq`와 `collection.immutable.Seq`가 같지 않아서 아래 `foldLeft` 컴파일에서 타입 오류 생김)
    2. `Future.fold`를 `Future.foldLeft`로 변경. `Future.fold`는 사용금지예고(deprecated) 됨.

5. 주석 번역