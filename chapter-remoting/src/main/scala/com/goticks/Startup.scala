package com.goticks

import scala.concurrent.Future
import scala.util.{Failure, Success}

import akka.actor.ActorSystem
import akka.event.Logging

import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.server.Route

import akka.stream.ActorMaterializer

trait Startup extends RequestTimeout {
  def startup(api: Route)(implicit system: ActorSystem) = {
    val host = system.settings.config.getString("http.host") // 설정에서 호스트와 포트를 가져온다
    val port = system.settings.config.getInt("http.port")
    startHttpServer(api, host, port)
  }

  def startHttpServer(api: Route, host: String, port: Int)
      (implicit system: ActorSystem) = {
    implicit val ec = system.dispatcher  // bindAndHandle에는 암시적인 ExecutionContext가 필요하다
    implicit val materializer = ActorMaterializer()
    val bindingFuture: Future[ServerBinding] =
    Http().bindAndHandle(api, host, port) // HTTP 서버를 시작한다
   
    val log = Logging(system.eventStream, "go-ticks")
    bindingFuture.map { serverBinding =>
      log.info(s"RestApi bound to ${serverBinding.localAddress} ")
    }.onComplete { 
      case Success(v) =>
	  case Failure(ex) =>
        log.error(ex, "Failed to bind to {}:{}!", host, port)
        system.terminate()
    }
  }
}
