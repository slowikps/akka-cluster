package advanced

import advanced.health.ClusterStatusActor
import akka.actor.ActorSystem
import akka.util.Timeout

import scala.annotation.tailrec
import scala.concurrent.duration._
import scala.io.StdIn
import scala.language.postfixOps
object Main extends App {

  import akka.pattern.ask
  implicit val timeout: Timeout = 1 second

  val system = ActorSystem("slowikps-system")

  val healthSingletonManager = ClusterStatusActor.createSingletonManager(system)
  val healthSingletonProxy = ClusterStatusActor.createSingletonProxy(system)


  commandLineParserLoop()

  @tailrec
  def commandLineParserLoop(): Unit = {
    import scala.concurrent.ExecutionContext.Implicits.global
    StdIn.readLine() match {
      case "s" | "status" => {
        println("Requesting status")
        (healthSingletonProxy ? ClusterStatusActor.PrintStatus).map(println)
      }
      case _              => println("unknown command")
    }
    commandLineParserLoop()
  }
}

//-Dakka.remote.netty.tcp.port=2551 -Dakka.cluster.roles.0=game-engine"),
