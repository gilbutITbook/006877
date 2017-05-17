package aia.next

import akka.actor._
import akka.testkit._

class BasketSpec extends PersistenceSpec(ActorSystem("test"))
    with PersistenceCleanup {

  val shopperId = 5L
  val macbookPro = Item("Apple Macbook Pro", 1, BigDecimal(2499.99))
  val displays = Item("4K Display", 3, BigDecimal(2499.99))

  "The basket" should {
    "return the items" in {
      val basket = system.actorOf(Basket.props, Basket.name(shopperId))
      basket ! Basket.Add(macbookPro, shopperId)
      basket ! Basket.Add(displays, shopperId)

      basket ! Basket.GetItems(shopperId)
      //basket ! Basket.GetItems
      expectMsg(Items(macbookPro, displays))
      killActors(basket)
    }

    "return the items in a typesafe way" in {
      import akka.typed._
      import akka.typed.scaladsl.AskPattern._
      import scala.concurrent.Future
      import scala.concurrent.duration._
	  import scala.concurrent.Await
      
      implicit val timeout = akka.util.Timeout(1 second)

      val macbookPro =
        TypedBasket.Item("Apple Macbook Pro", 1, BigDecimal(2499.99))
      val displays =
        TypedBasket.Item("4K Display", 3, BigDecimal(2499.99))

	  // TypedBasket.basketBehavior가 메서드로 변경됐기 때문에
	  // ()를 붙여서 호출해 줘야 한다.
	  // 예전의 deprecated된 DSL을 사용하는
	  // val로 정의된 TypedBasket.basketBehavior를 
	  // 사용하고 싶다면 basketBehavior()에서 ()를 
	  // 없애면 된다.
      val sys: ActorSystem[TypedBasket.Command] =
        ActorSystem("typed-basket", TypedBasket.basketBehavior())
      sys ! TypedBasket.Add(macbookPro, shopperId)
      sys ! TypedBasket.Add(displays, shopperId)

      implicit def scheduler = sys.scheduler
      val items: Future[TypedBasket.Items] =
        sys ? (TypedBasket.GetItems(shopperId, _))

      val res = Await.result(items, 10 seconds)
      res should equal(TypedBasket.Items(Vector(macbookPro, displays)))
      //sys ? Basket.GetItems
      sys.terminate()
    }

  }
}
