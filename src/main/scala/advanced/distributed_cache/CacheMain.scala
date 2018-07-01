package advanced.distributed_cache

import java.net.Socket

import advanced.health.ClusterStatusActor
import akka.actor.ActorSystem
import akka.serialization.SerializationExtension
import akka.util.Timeout
import akka.actor.ActorSystem
import akka.cluster.Cluster
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer

import scala.io.StdIn
import scala.annotation.tailrec
import scala.concurrent.Future
import scala.io.StdIn
import scala.concurrent.duration._
import akka.actor.Address
import com.typesafe.config.ConfigFactory

import scala.util.{Failure, Success, Try}

object CacheMain extends App {

  import akka.pattern.ask
  implicit val timeout: Timeout = 1 second

  implicit val system = Try {
    val mainSystemConfig = ConfigFactory
      .parseString("akka.remote.netty.tcp.port=2551")
      .withFallback(ConfigFactory.load())
    ActorSystem("slowikps-system", mainSystemConfig)
  } match {
    case Success(system) => system
    case Failure(ex) => ActorSystem("slowikps-system")
  }

  implicit val executionContext = system.dispatcher

  //  val cluster             = Cluster(system)
  //  val list: List[Address] = ??? //your method to dynamically get seed nodes
  //  cluster.joinSeedNodes(list)

  val defaultAddress = SerializationExtension(system).system.provider.getDefaultAddress
  def setupHttpLayer(): Future[Http.ServerBinding] = {
    implicit val materializer = ActorMaterializer()

    val route =
      pathPrefix("hello") {
        get {
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http</h1>"))
        }
      } ~ path(Segment / Segment) { (key, value) =>
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"<h1>key: $key, value: $value"))
      }

    Http()
      .bindAndHandle(route, "localhost", 8080)
      .recoverWith {
        case ex => Http().bindAndHandle(route, "localhost", 0)
      }
  }

  println(s"Actor system: start: $defaultAddress, port: ${defaultAddress.port}")

  val bindingFuture = setupHttpLayer()
  bindingFuture.foreach(sb => println(s"Server online at http://localhost:${sb.localAddress}/\nPress RETURN to stop..."))

  StdIn.readLine() // let it run until user presses return
  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => system.terminate())
  // and shutdown when done
}
