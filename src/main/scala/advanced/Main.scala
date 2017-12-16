package advanced

import java.util.concurrent.TimeUnit

import advanced.health.ClusterStatusActor
import akka.actor.ActorSystem
import akka.util.Timeout

import scala.concurrent.duration._
import scala.language.postfixOps
object Main extends App {

  import akka.pattern.ask

  val system = ActorSystem("slowikps-system")

  val healthSingletonManager = ClusterStatusActor.createSingletonManager(system)
  val healthSingletonProxy = ClusterStatusActor.createSingletonProxy(system)

  TimeUnit.MILLISECONDS.sleep(100)

  implicit val timeout: Timeout = 1 second
  import scala.concurrent.ExecutionContext.Implicits.global
  val status = healthSingletonProxy ? ClusterStatusActor.PrintStatus

  status.map(println)
  println("Message sent")
}
