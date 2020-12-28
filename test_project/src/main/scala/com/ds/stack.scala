package com.ds

import scala.concurrent.Future
import akka.actor
import akka.actor.{Actor, Props}

object myStack{
	def props(data: Int) = Props(new myStack)

	case class pop
	case class push(data: Int)
}


class myStack extends Actor{
	import myStack._

	var dataset = Vector.empty[Int] //data structure

	def receive = { 
		case pop => 
			 

		case push(data) =>
			dataset = dataset ++ data
	}	 

}