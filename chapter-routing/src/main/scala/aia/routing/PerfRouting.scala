package aia.routing

import akka.actor._
import akka.routing._

class TestSuper() extends Actor {
  def receive = {
    case "OK" =>
    case _ => throw new IllegalArgumentException("not supported")
  }

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    super.preRestart(reason, message)
    println("restart %s".format(self.path.toString))
  }
}

class GetLicenseCreator(nrActors: Int, nextStep: ActorRef) extends Actor {
  var createdActors = Seq[ActorRef]()

  override def preStart(): Unit = {
    super.preStart()
    createdActors = (0 until  nrActors).map(nr => {
      context.actorOf(Props(new GetLicense(nextStep)), "GetLicense"+nr)
    })
  }

  def receive = {
    case "KillFirst" => {
      createdActors.headOption.foreach(_ ! Kill)
      createdActors = createdActors.tail
    }
    case _ => throw new IllegalArgumentException("not supported")
  }

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    super.preRestart(reason, message)
    println("restart %s".format(self.path.toString))
  }
}

class GetLicenseCreator2(nrActors: Int, nextStep: ActorRef) extends Actor {
  // 자식을 재시작한다
  override def preStart(): Unit = {
    super.preStart()
    (0 until  nrActors).map(nr => {
      val child = context.actorOf(Props(new GetLicense(nextStep)), "GetLicense"+nr)
      context.watch(child)
    })
  }

  def receive = {
    case "KillFirst" => {
      if(!context.children.isEmpty) {
        context.children.head ! PoisonPill
      }
    }
    case Terminated(child) => {
      val newChild = context.actorOf(Props(new GetLicense(nextStep)), child.path.name)
      context.watch(newChild)
    }
  }

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    super.preRestart(reason, message)
    println("restart %s".format(self.path.toString))
  }
}

case class PreferredSize(size: Int)

class WrongDynamicRouteeSizer(nrActors: Int, props: Props, router: ActorRef) extends Actor {
  var nrChildren = nrActors

  // 자식을 재시작한다
  override def preStart(): Unit = {
    super.preStart()
    (0 until  nrChildren).map(nr => createRoutee())
  }

  def createRoutee(): Unit = {
    val child = context.actorOf(props)
    router ! AddRoutee(ActorRefRoutee(child))
  }

  def receive = {
    case PreferredSize(size) => {
      if (size < nrChildren) {
        // 삭제하기
        println("Delete %d children".format(nrChildren - size))
        context.children.take(nrChildren - size).foreach(ref => {
          println("delete: "+ ref)
          router ! RemoveRoutee(ActorRefRoutee(ref))
        })
        router ! GetRoutees
      } else {
        (nrChildren until size).map(nr => createRoutee())
      }
      nrChildren = size
    }
    case routees: Routees => {
      import collection.JavaConverters._
      val active = routees.getRoutees.asScala.map{
        case x: ActorRefRoutee => x.ref.path.toString
        case x: ActorSelectionRoutee => x.selection.pathString
      }
      println("Active: "+ active)
      val notUsed = context.children.filterNot(routee => active.contains(routee.path.toString))
      println("Not used: "+ notUsed)
      notUsed.foreach(context.stop(_))
    }
  }

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    super.preRestart(reason, message)
    println("restart %s".format(self.path.toString))
  }
}


class DynamicRouteeSizer(nrActors: Int,
                         props: Props,
                         router: ActorRef) extends Actor {
  var nrChildren = nrActors
  var childInstanceNr = 0

  // 자식을 재시작한다
  override def preStart(): Unit = {
    super.preStart()
    (0 until  nrChildren).map(nr => createRoutee())
  }

  def createRoutee(): Unit = {
    childInstanceNr += 1
    val child = context.actorOf(props, "routee" + childInstanceNr)
    val selection = context.actorSelection(child.path)
    router ! AddRoutee(ActorSelectionRoutee(selection))
    context.watch(child)
  }

  def receive = {
    case PreferredSize(size) => {
      if (size < nrChildren) {
        // 삭제하기
        context.children.take(nrChildren - size).foreach(ref => {
          val selection = context.actorSelection(ref.path)
          router ! RemoveRoutee(ActorSelectionRoutee(selection))
        })
        router ! GetRoutees
      } else {
        (nrChildren until size).map(nr => createRoutee())
      }
      nrChildren = size
    }
    case routees: Routees => {
      // 라우티를 액터 경로로 변환한다
      import collection.JavaConverters._
      val active = routees.getRoutees.asScala.map{
        case x: ActorRefRoutee => x.ref.path.toString
        case x: ActorSelectionRoutee => x.selection.pathString
      }
      // 라우티 리스트를 처리한다
      for(routee <- context.children) {
        val index = active.indexOf(routee.path.toStringWithoutAddress)
        if (index >= 0) {
          active.remove(index)
        } else {
          // 자식 액터를 더이상 라우터가 사용하지 않는다
          routee ! PoisonPill
        }
      }
      // active에는 종료된 라우티들이 들어있다
      for (terminated <- active) {
        val name = terminated.substring(terminated.lastIndexOf("/")+1)
        val child = context.actorOf(props, name)
        context.watch(child)
      }
    }
    case Terminated(child) => router ! GetRoutees
  }
}


class DynamicRouteeSizer2(nrActors: Int, props: Props, router: ActorRef) extends Actor {
  var nrChildren = nrActors

  // 자식을 재시작한다
  override def preStart(): Unit = {
    super.preStart()
    (0 until  nrChildren).map(nr => createRoutee())
  }

  def createRoutee(): Unit = {
    val child = context.actorOf(props)
    val selection = context.actorSelection(child.path)
    router ! AddRoutee(ActorSelectionRoutee(selection))
    context.watch(child)
    println("Add routee "+ child)
  }

  def receive = {
    case PreferredSize(size) => {
      val currentNumber = context.children.size
      if (size < currentNumber) {
        // 삭제하기
        println("Delete %d children".format(currentNumber - size))
        context.children.take(currentNumber - size).foreach(ref => {
          println("delete: "+ ref)
          context.stop(ref)
        })
      } else {
        (currentNumber until size).map(nr => createRoutee())
      }
      nrChildren = size
    }
    case routees: Routees => {

      println("routees " + routees)
      if(routees.getRoutees.size() < nrChildren) {
        ( routees.getRoutees.size() until nrChildren).map(nr => createRoutee())
      }

    }
    case Terminated(child) => {
      println("Terminated " + child)
      val selection = context.actorSelection(child.path)
      router ! RemoveRoutee(ActorSelectionRoutee(selection))
      router ! GetRoutees
    }
  }

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    super.preRestart(reason, message)
    println("restart %s".format(self.path.toString))
  }
}
