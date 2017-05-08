package aia.channels.enshahar

// multi-jvm:test-only aia.channels.enshahar.AtLeastOnceDeliverySampleSpec으로 시작할것

import org.scalatest.{WordSpecLike, BeforeAndAfterAll, MustMatchers}
import akka.testkit.ImplicitSender
import akka.actor.{Props, Actor, ActorSelection}
import akka.persistence._

/**
 *스칼라테스트의 MultiNodeSpec 사용
 */

import akka.remote.testkit.MultiNodeSpecCallbacks
import akka.remote.testkit.MultiNodeConfig
import akka.remote.testkit.MultiNodeSpec

trait STMultiNodeSpec
  extends MultiNodeSpecCallbacks
  with WordSpecLike
  with MustMatchers
  with BeforeAndAfterAll {

  override def beforeAll() = multiNodeSpecBeforeAll()

  override def afterAll() = multiNodeSpecAfterAll()
}


object AtLeastOnceDeliverySampleConfig extends MultiNodeConfig {
  val client = role("Client")
  val server = role("Server")
  testTransport(on = true)
}

class AtLeastOnceDeliverySampleSpecMultiJvmNode1 extends AtLeastOnceDeliverySample
class AtLeastOnceDeliverySampleSpecMultiJvmNode2 extends AtLeastOnceDeliverySample

import akka.remote.transport.ThrottlerTransportAdapter.Direction
import scala.concurrent.duration._
import concurrent.Await
import akka.persistence.AtLeastOnceDelivery

case class Msg(deliveryId: Long, s: String)
case class Confirm(deliveryId: Long)
 
sealed trait Evt
case class MsgSent(s: String) extends Evt
case class MsgConfirmed(deliveryId: Long) extends Evt

class AtLeastOnceDeliveryActor(destination: ActorSelection)
  extends PersistentActor with AtLeastOnceDelivery {
 
  override def persistenceId: String = "persistence-id"
 
  override def receiveCommand: Receive = {
    case s: String           => { print(s"XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXstring received: $s"); persist(MsgSent(s))(updateState) }
    case Confirm(deliveryId) => persist(MsgConfirmed(deliveryId))(updateState)
  }
 
  override def receiveRecover: Receive = {
    case evt: Evt => updateState(evt)
  }
 
  def updateState(evt: Evt): Unit = evt match {
    case MsgSent(s) =>
      deliver(destination)(deliveryId => Msg(deliveryId, s))
 
    case MsgConfirmed(deliveryId) => confirmDelivery(deliveryId)
  }
}

class AtLeastOnceDeliverySample
  extends MultiNodeSpec(AtLeastOnceDeliverySampleConfig)
  with STMultiNodeSpec
  with ImplicitSender {

  import AtLeastOnceDeliverySampleConfig._

  def initialParticipants = roles.size

  "A MultiNodeSample" must {

    "wait for all nodes to enter a barrier" in {
      enterBarrier("startup")
    }

    "send to and receive from a remote node" in {
      runOn(client) {
        enterBarrier("deployed")
        val pathToEcho = node(server) / "user" / "echoecho"
        val echo = system.actorSelection(pathToEcho)
        val sender = system.actorOf(Props(new AtLeastOnceDeliveryActor(echo)))
		
        sender ! "message1"
        expectMsg("message1")
        Await.ready(
          testConductor.blackhole( client, server, Direction.Both),
          1 second)

        echo ! "DirectMessage"
        sender ! "ProxyMessage"
        expectNoMsg(3 seconds)

        Await.ready(
          testConductor.passThrough( client, server, Direction.Both),
          1 second)

        expectMsg("ProxyMessage")

        echo ! "DirectMessage2"
        expectMsg("DirectMessage2")
      }

      runOn(server) {
        system.actorOf(Props(new Actor {
          def receive = {
		    case Msg(deliveryId, s) =>
			  sender() ! Confirm(deliveryId)
			  sender() ! s
            case msg: AnyRef => {
              sender() ! msg
            }
          }
        }), "echoecho")
        enterBarrier("deployed")
      }

      enterBarrier("finished")
    }
  }
}

