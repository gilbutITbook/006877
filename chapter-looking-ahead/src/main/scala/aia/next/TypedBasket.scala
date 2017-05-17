package aia.next

import akka.typed._
/* deprecated된 DSL 사용하는 경우 다음을 임포트해야 한다
import akka.typed.ScalaDSL._ 
*/

// 아카 2.5에서 제안하는 대로 구현할 경우 필요한 import문
import akka.typed.scaladsl.Actor._

object TypedBasket {
  sealed trait Command {
    def shopperId: Long
  }

  final case class GetItems(shopperId: Long,
                            replyTo: ActorRef[Items]) extends Command
  final case class Add(item: Item, shopperId: Long) extends Command

  // a simplified version of Items and Item
  case class Items(list: Vector[Item]= Vector.empty[Item])
  case class Item(productId: String, number: Int, unitPrice: BigDecimal)

/* 
  원래 책의 소스코드다. 아카 2.4.14에서 정상작동하며, 2.5에서는 deprecated된 기능을 사용한다.
  val basketBehavior =
  ContextAware[Command] { ctx ⇒
    var items = Items()

    Static {
      case GetItems(productId, replyTo) =>
       replyTo ! items
      case Add(item, productId) =>
        items = Items(items.list :+ item)
      //case GetItems =>
    }
  }*/
  def basketBehavior(items: Items = Items()): Behavior[Command] = 
    Stateful[Command] { (ctx, msg) =>
	  msg match {
	    case GetItems(productId, replyTo) =>
          replyTo ! items
		  Same
	    case Add(item, productId) =>
          basketBehavior(Items(items.list :+ item))
	  }
	}
}

