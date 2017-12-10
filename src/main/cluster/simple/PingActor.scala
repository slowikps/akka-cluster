package sample.cluster.simple

import akka.actor.{Actor, ActorLogging, Props}
import akka.util.Timeout

import scala.concurrent.duration._
import scala.language.postfixOps

class PingActor extends Actor with ActorLogging {

  import scala.concurrent.ExecutionContext.Implicits.global


  implicit val timeout = Timeout(3.seconds)
  implicit val scheduler = context.system.scheduler

  scheduler.schedule(100 millis, 5 second)(self ! "Message: PONG")

  override def receive: Receive = {
    case x => log.info(s"Ping: $x")
  }
}

object PingActor {

  def props = Props[PingActor]
}
