# 의존관계 변경

1. `build.sbt` : 버전변경
  - `akka` 버전은 "2.5.0"으로
  - `akka-http`관련 버전은 "10.0.5"로
  - `scalatest`는 "3.0.1"로
  - `logback-classic`은 1.2.3로
  - `commons-io`는 "2.5"로
  - `leveldb`는 "0.8"로

2. `scala.sbt` : 스칼라 버전을 2.12.2로 변경,   "-Xlint:-unused,_", "-Ywarn-unused:imports" 추가, "-Ywarn-unused" 제거

3. `project/plugin.sbt`를 다음과 같이 변경(이중에 sbt-multi-jvm은 multi-jvm 테스트를 위한 것이고, sbt-multi-jvm가 필요한 이유는 AtLeastOnceMultiJvm 관련 내용을 한글판에 추가했기 때문임)

    resolvers += Classpaths.typesafeReleases
    
    addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.13.0")
    
    addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.0.0")
    
    addSbtPlugin("com.typesafe.sbt" % "sbt-multi-jvm" % "0.3.11")

4. `project\WordsBuild.scala` 추가(multi-jvm 테스트를 위한 것임)

```scala
import sbt._
import Keys._
import com.typesafe.sbt.SbtMultiJvm
import com.typesafe.sbt.SbtMultiJvm.MultiJvmKeys.{ MultiJvm }

object WordsBuild extends Build {

  lazy val buildSettings = Defaults.defaultSettings ++ multiJvmSettings ++ Seq(
    crossPaths   := false
  )

  lazy val project = Project(
    id = "persistence",
    base = file("."),
    settings = buildSettings ++ Project.defaultSettings
  ) configs(MultiJvm)

  lazy val multiJvmSettings = SbtMultiJvm.multiJvmSettings ++ Seq(
    // make sure that MultiJvm test are compiled by the default test compilation
    compile in MultiJvm <<= (compile in MultiJvm) triggeredBy (compile in Test),
    // disable parallel tests
    parallelExecution in Test := false,
    // make sure that MultiJvm tests are executed by the default test target
    executeTests in Test <<=
      ((executeTests in Test), (executeTests in MultiJvm)) map {
        case ((testResults), (multiJvmResults)) =>
          val overall =
            if (testResults.overall.id < multiJvmResults.overall.id)
              multiJvmResults.overall
            else
              testResults.overall
          Tests.Output(overall,
            testResults.events ++ multiJvmResults.events,
            testResults.summaries ++ multiJvmResults.summaries)
      }
  )
}
```

5. `src\main\scala\aia\persistence\rest\ShoppersServiceSupport.scala`의 35번째 줄에서 `onFailure`를 `failed.foreach`로 변경

6. http://doc.akka.io/docs/akka/2.5/project/migration-guide-2.4.x-2.5.x.html#ExtensionKey_Deprecation 에 맞춰 `src\main\scala\aia\persistence\Settings.scala`의 `ExtensionKey` 관련 구현을 변경함

예전: (deprecated됨)

```scala
    object MyExtension extends ExtensionKey[MyExtension]
```
	
새 버전:

```scala
    object MyExtension extends ExtensionId[MyExtension] with ExtensionIdProvider {
      
      override def lookup = MyExtension
     
      override def createExtension(system: ExtendedActorSystem): MyExtension =
        new MyExtension(system)
     
      // needed to get the type right when used from Java
      override def get(system: ActorSystem): MyExtension = super.get(system)
    }
```
	
7. 사용 안되는 import나 패턴매치에서 사용되지 않는 불필요한 변수 등 제거 

8. `src/multi-jvm/resources/reference.conf`와 `scala/aia/persistence/enshahar/AtLeastOnceMultiJvmSpec.scala`를 추가
