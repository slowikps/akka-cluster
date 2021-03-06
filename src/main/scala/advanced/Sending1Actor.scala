package advanced

import akka.actor.{Actor, Props}

object Sending1Actor {
  def props: Props = Props(new Sending1Actor)
}

class Sending1Actor extends Actor {

  override def receive = {
    case _ => sender() ! 1
  }
}
