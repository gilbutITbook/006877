package com.ds

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.StatusCodes.{BadRequest, OK}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._



class RestApi(system: ActorSystem) extends Rest{
	def createStack = system.actorof(myStack.Props)
}
trait Rest extends StackApi{

	def route = stackRoute ~ stackPostRout

	//get/post route
	def stackRoute = 
		pathPrefix("stack"){
			pathEndOrSingleSlash{
				get{
					onSuccess(pop()){ ev =>
						case a => complete(OK)
						case b => complete(BadRequest)
					}
				}
			}
		}

	def stackPostRout = 
		pathPrefix("stack" / Segment){ data =>
			pathEndOrSingleSlash{
				post{
					println("post!")
					val idata = data.toInt
					onSuccess(push(idata)){
						complete(OK)
					}
				}
			}
		}
}

trait StackApi{
	import myStack._

	def createStack(): ActorRef

	val stack = createStack()

	def pop() = 
	def push(value: Int) = 

}
