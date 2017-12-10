package advanced

import advanced.health.ClusterStatus
import akka.actor.ActorSystem

object Main extends App {


  val system = ActorSystem("slowikps-system")

  val healthSingletonManager = ClusterStatus.createSingletonManager(system)
  val healthSingletonProxy = ClusterStatus.createSingletonProxy(system)

  healthSingletonProxy ! ClusterStatus.PrintStatus

  println("Message sent")
}
