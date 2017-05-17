package aia.persistence

import akka.actor._

import aia.persistence.rest.ShoppersServiceSupport

object SingletonMain extends App with ShoppersServiceSupport {
  implicit val system = ActorSystem("shoppers")
  val shoppers = system.actorOf(ShoppersSingleton.props,
   ShoppersSingleton.name)
  startService(shoppers)
}
