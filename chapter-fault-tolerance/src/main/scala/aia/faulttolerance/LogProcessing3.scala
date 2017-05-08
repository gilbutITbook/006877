package aia.faulttolerance

import akka.actor._
import java.io.File
import akka.actor.SupervisorStrategy.{ Stop, Resume, Restart }
import akka.actor.OneForOneStrategy
import scala.concurrent.duration._
import language.postfixOps

package dbstrategy3 {

  object LogProcessingApp extends App {
    val sources = Vector("file:///source1/", "file:///source2/")
    val system = ActorSystem("logprocessing")
    // Props와 의존관계를 정의한다 
    val databaseUrl = "http://mydatabase"
    
    val writerProps = Props(new DbWriter(databaseUrl))
    val dbSuperProps = Props(new DbSupervisor(writerProps))
    val logProcSuperProps = Props(
      new LogProcSupervisor(dbSuperProps))
    val topLevelProps = Props(new FileWatcherSupervisor(
      sources,
      logProcSuperProps))
    system.actorOf(topLevelProps)
  }



  class FileWatcherSupervisor(sources: Vector[String],
                               logProcSuperProps: Props)
    extends Actor {

    var fileWatchers: Vector[ActorRef] = sources.map { source =>
      val logProcSupervisor = context.actorOf(logProcSuperProps)
      val fileWatcher = context.actorOf(Props(
        new FileWatcher(source, logProcSupervisor)))
      context.watch(fileWatcher)
      fileWatcher
    }

    override def supervisorStrategy = AllForOneStrategy() {
      case _: DiskError => Stop
    }

    def receive = {
      case Terminated(fileWatcher) =>
        fileWatchers = fileWatchers.filterNot(w => w == fileWatcher)
        if (fileWatchers.isEmpty) self ! PoisonPill
    }
  }



  class FileWatcher(sourceUri: String,
                    logProcSupervisor: ActorRef)
    extends Actor with FileWatchingAbilities {
    register(sourceUri)

    import FileWatcherProtocol._
    import LogProcessingProtocol._

    def receive = {
      case NewFile(file, _) =>
        logProcSupervisor ! LogFile(file)
      case SourceAbandoned(uri) if uri == sourceUri =>
        self ! PoisonPill
    }
  }



  class LogProcSupervisor(dbSupervisorProps: Props)
    extends Actor {
    override def supervisorStrategy = OneForOneStrategy() {
      case _: CorruptedFileException => Resume
    }
    val dbSupervisor = context.actorOf(dbSupervisorProps)
    val logProcProps = Props(new LogProcessor(dbSupervisor))
    val logProcessor = context.actorOf(logProcProps)

    def receive = {
      case m => logProcessor forward (m)
    }
  }



  class LogProcessor(dbSupervisor: ActorRef)
    extends Actor with LogParsing {
    import LogProcessingProtocol._
    def receive = {
      case LogFile(file) =>
        val lines = parse(file)
        lines.foreach(dbSupervisor ! _)
    }
  }


  class DbImpatientSupervisor(writerProps: Props) extends Actor {
    override def supervisorStrategy = OneForOneStrategy(
      maxNrOfRetries = 5,
      withinTimeRange = 60 seconds) {
        case _: DbBrokenConnectionException => Restart
      }
    val writer = context.actorOf(writerProps)
    def receive = {
      case m => writer forward (m)
    }
  }



  class DbSupervisor(writerProps: Props) extends Actor {
    override def supervisorStrategy = OneForOneStrategy() {
      case _: DbBrokenConnectionException => Restart
    }
    val writer = context.actorOf(writerProps)
    def receive = {
      case m => writer forward (m)
    }
  }



  class DbWriter(databaseUrl: String) extends Actor {
    val connection = new DbCon(databaseUrl)

    import LogProcessingProtocol._
    def receive = {
      case Line(time, message, messageType) =>
        connection.write(Map('time -> time,
          'message -> message,
          'messageType -> messageType))
    }
  }

  class DbCon(url: String) {
    /**
     * map을 데이터베이스에 기록한다.
     * @param map 데이터베이스에 기록할 맵이다.
     * @throws DbBrokenConnectionException 연결이 깨진 경우 발생한다. 나중에 연결이 돌아올 수도 있다.
     * @throws DbNodeDownException 데이터베이스 노드가 데이터베이스 클러스터에서 제거된 경우 발생한다. 결코 노드가 다시 정상적동하게 되는 일은 없다.
     */
    def write(map: Map[Symbol, Any]):Unit = {
      //
    }
    def close(): Unit = {
      //
    }
  }

  @SerialVersionUID(1L)
  class DiskError(msg: String)
    extends Error(msg) with Serializable

  @SerialVersionUID(1L)
  class CorruptedFileException(msg: String, val file: File)
    extends Exception(msg) with Serializable

  @SerialVersionUID(1L)
  class DbBrokenConnectionException(msg: String)
    extends Exception(msg) with Serializable


  trait LogParsing {
    import LogProcessingProtocol._
    // 로그 파일을 파싱한다. 로그 파일의 각 줄에서 Line 객체를 만든다.
    // 파일이 잘못된 경우에는 CorruptedFileException 예외를 던진다.
    def parse(file: File): Vector[Line] = {
      // 파서를 여기서 정의한다. 지금은 일단 더미 값을 반환한다.
      Vector.empty[Line]
    }
  }
  object FileWatcherProtocol {
    case class NewFile(file: File, timeAdded: Long)
    case class SourceAbandoned(uri: String)
  }
  trait FileWatchingAbilities {
    def register(uri: String): Unit = {

    }
  }


  object LogProcessingProtocol {
    // 새로운 로그 파일을 표현한다.
    case class LogFile(file: File)
    // LogProcessor가 파싱한 로그 파일의 한 줄을 표현한다.
    case class Line(time: Long, message: String, messageType: String)
  }


}
