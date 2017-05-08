# 의존관계 변경

1. `build.sbt` : `akka`관련 프로젝트 버전은 2.5.0으로, `akka-http` 버전은 "10.0.5"으로, `akka-http-experimental`과 `akka-http-spray-json-experimental`은 `-experimental`을 없애고 `akka-http`와 `akka-http-spray-json`으로 변경, `logback-classic` 버전은 1.2.3으로 변경

2. `scala.sbt` : 스칼라 버전을 2.12.2로 변경

3. `src/main/scala/com/goticks/Main.scala`: 39행 근처의  `onFailure`

    Main.scala:39: method onFailure in trait Future is deprecated (since 2.12.0): use `onComplete` or `failed.foreach` instead (keep in mind that they take total rather than partial functions)

`Future`의 `onFailure`가 사용금지선언(deprecated)되었으므로, `onComplete`에 `Success`/`Failure` 매치(`Try` 모나드 사용)를 하도록 변경

4. `src\test\scala\com\goticks\TicketSellerSpec.scala` :  61행

    match not exhaustive

`(_, _)` 이면 실패 할 수 있음이라는 메시지 나오는데, catch-all 절 추가해서 오류 없앰
 
5. `"com.typesafe.akka" %% "akka-stream"      % akkaVersion,` 의존관계 추가

이 의존관계가 없으면 `akka-stream`이 `akka-actor`의 기존 버전(2.4.17)을 참조해서 jar파일 패키징을 실행하면 `Method Not Found` 오류가 남.

6. Procfile.window에 헤로쿠 배치파일에 대한 경로를 추가함. 로컬 실행하고 싶다면 `heroku local web -f Procfile.windows`라고 윈도우즈에서 실행해야 함

7. 커맨트 한글화

 