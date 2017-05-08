# 의존관계 변경

1. `build.sbt` : `akka` 버전은 "2.5.0"으로, `akka-http`관련 버전은 "10.0.5"로, `scalatest`는 "3.0.1"로 번경, `nscala-time`은 2.16.0로, `logback-classic`은 1.2.3으로 변경. 다음을 맨 아래 추가


    // Assembly 설정
    mainClass in Compile := Some("com.goticks.SingleNodeMain")
    
    assemblyJarName in assembly := "goticks-server.jar"
    
    enablePlugins(JavaAppPackaging)

2. `scala.sbt` : 스칼라 버전을 2.12.2로 변경

3. `project\build.properties`: 변경
    sbt.version=0.13.15

4. `src\main\scala\com\goticks\Startup.scala`: 31번째 줄. 
 
   `Future`의 `onFailure`가 사용금지선언(deprecated)되었으므로, `onComplete`에 `Success`/`Failure` 매치(`Try` 모나드 사용)를 하도록 변경

5. `src\test\scala\com\goticks\TicketSellerSpec.scala`의 "Sell tickets in batches until they are sold out"  테스케이스 61번째 줄

    match may not be exhautive 를 방지하기 위해 catch all case (case _ => ) 추가
	
6. `src\main\scala\com\goticks\RemoveLookupProxy.scala` 41번째 줄: 문자열 인터폴레이션을 위해 ""앞에 s 추가

7. `src\main\scala\com\goticks\RemoteBoxOfficeForwarder.scala` 43 번째 줄: 문자열 인터폴레이션을 위해 ""앞에 s 추가

8. `Procfile`내용을 다음과 같이 변경. `Procfile.win`도 만들어서 배치파일 실행하게 설정

    web: target/universal/stage/bin/goticks