package aia.faulttolerance

import java.io.File
import java.util.UUID
import akka.actor._
import akka.actor.SupervisorStrategy.{ Stop, Resume, Restart, Escalate }
import akka.actor.OneForOneStrategy
import scala.concurrent.duration._
import language.postfixOps

package dbstrategy1 {

  object LogProcessingApp extends App {
    val sources = Vector("file:///source1/", "file:///source2/")
    val system = ActorSystem("logprocessing")

    val databaseUrl = "http://mydatabase1"
    
    system.actorOf(
      LogProcessingSupervisor.props(sources, databaseUrl), 
      LogProcessingSupervisor.name
    )
  }

  object LogProcessingSupervisor {
    def props(sources: Vector[String], databaseUrl: String) =
      Props(new LogProcessingSupervisor(sources, databaseUrl))
    def name = "file-watcher-supervisor" 
  }

  class LogProcessingSupervisor(
    sources: Vector[String], 
    databaseUrl: String
  ) extends Actor with ActorLogging {

    override def supervisorStrategy = OneForOneStrategy() {
      case _: CorruptedFileException => Resume
      case _: DbBrokenConnectionException => Restart
      case _: DiskError => Stop
    }

    var fileWatchers = sources.map { source =>
      val dbWriter = context.actorOf(
        DbWriter.props(databaseUrl), 
        DbWriter.name(databaseUrl)
      )      

      val logProcessor = context.actorOf(
        LogProcessor.props(dbWriter), 
        LogProcessor.name
      )   

      val fileWatcher = context.actorOf(
        FileWatcher.props(source, logProcessor),
        FileWatcher.name
      )
      context.watch(fileWatcher)
      fileWatcher
    }


    def receive = {
      case Terminated(actorRef) =>
        if(fileWatchers.contains(actorRef)) {
          fileWatchers = fileWatchers.filterNot(_ == actorRef)
          if (fileWatchers.isEmpty) {
            log.info("Shutting down, all file watchers have failed.")
            context.system.terminate()
          }
        }
    }
  }
  
  object FileWatcher {
   def props(source: String, logProcessor: ActorRef) = 
     Props(new FileWatcher(source, logProcessor))
   def name = s"file-watcher-${UUID.randomUUID.toString}"
   case class NewFile(file: File, timeAdded: Long)
   case class SourceAbandoned(uri: String)
  }

  class FileWatcher(source: String,
                    logProcessor: ActorRef)
    extends Actor with FileWatchingAbilities {
    register(source)
    
    import FileWatcher._

    def receive = {
      case NewFile(file, _) =>
        logProcessor ! LogProcessor.LogFile(file)
      case SourceAbandoned(uri) if uri == source =>
        self ! PoisonPill
    }
  }
  
  object LogProcessor {
    def props(dbWriter: ActorRef) = 
      Props(new LogProcessor(dbWriter))
    def name = s"log_processor_${UUID.randomUUID.toString}"
    // 새로운 로그 파일을 표현한다.
    case class LogFile(file: File)
  }

  class LogProcessor(dbWriter: ActorRef)
    extends Actor with ActorLogging with LogParsing {

    import LogProcessor._

    def receive = {
      case LogFile(file) =>
        val lines: Vector[DbWriter.Line] = parse(file)
        lines.foreach(dbWriter ! _)
    }
  }

  object DbWriter  {
    def props(databaseUrl: String) =
      Props(new DbWriter(databaseUrl))
    def name(databaseUrl: String) =
      s"""db-writer-${databaseUrl.split("/").last}"""

    // LogProcessor가 파싱한 로그 파일의 한 줄을 표현한다.
    case class Line(time: Long, message: String, messageType: String)
  }

  class DbWriter(databaseUrl: String) extends Actor {
    val connection = new DbCon(databaseUrl)

    import DbWriter._
    def receive = {
      case Line(time, message, messageType) =>
        connection.write(Map('time -> time,
          'message -> message,
          'messageType -> messageType))
    }

    override def postStop(): Unit = {
      connection.close() 
    }
  }

  class DbCon(url: String) {
     /**
     * map을 데이터베이스에 기록한다.
     * @param map 데이터베이스에 기록할 맵이다.
     * @throws DbBrokenConnectionException 연결이 깨진 경우 발생한다. 나중에 연결이 돌아올 수도 있다.
     * @throws DbNodeDownException 데이터베이스 노드가 데이터베이스 클러스터에서 제거된 경우 발생한다. 결코 노드가 다시 정상작동하게 되는 일은 없다.
     */
    def write(map: Map[Symbol, Any]): Unit =  {
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

  @SerialVersionUID(1L)
  class DbNodeDownException(msg: String)
    extends Exception(msg) with Serializable

  trait LogParsing {
    import DbWriter._
    // 로그 파일을 파싱한다. 로그 파일의 각 줄에서 Line 객체를 만든다.
    // 파일이 잘못된 경우에는 CorruptedFileException 예외를 던진다.
    def parse(file: File): Vector[Line] = {
      // 파서를 여기서 정의한다. 지금은 일단 더미 값을 반환한다.
      Vector.empty[Line]
    }
  }

  trait FileWatchingAbilities {
    def register(uri: String): Unit = {

    }
  }
}
