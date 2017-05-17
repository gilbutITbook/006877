package aia.persistence

import scala.concurrent.duration._

import akka.actor._

import com.typesafe.config.Config

// deprecated됨
//object Settings extends ExtensionKey[Settings]

// 변경된 버전
// http://doc.akka.io/docs/akka/2.5/project/migration-guide-2.4.x-2.5.x.html#ExtensionKey_Deprecation 참조
//
object Settings extends ExtensionId[Settings] with ExtensionIdProvider {
  
  override def lookup = Settings
 
  override def createExtension(system: ExtendedActorSystem): Settings =
    new Settings(system)
 
  // 자바에서 사용할 때 타입을 제대로 얻게 하기 위한 메서드
  override def get(system: ActorSystem): Settings = super.get(system)
}
	
class Settings(config: Config) extends Extension {
  def this(system: ExtendedActorSystem) = this(system.settings.config)

  val passivateTimeout = Duration(config.getString("passivate-timeout"))
  object http {
    val host = config.getString("http.host")
    val port = config.getInt("http.port")
  }
}
