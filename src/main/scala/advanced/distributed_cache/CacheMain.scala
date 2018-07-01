package advanced.distributed_cache

import advanced.health.ClusterStatusActor
import akka.actor.ActorSystem
import akka.serialization.SerializationExtension
import akka.util.Timeout
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import scala.io.StdIn
import scala.annotation.tailrec
import scala.io.StdIn
import scala.concurrent.duration._

object CacheMain extends App {

  import akka.pattern.ask
  implicit val timeout: Timeout = 1 second

  implicit val system = ActorSystem("slowikps-system")
  val defaultAddress  = SerializationExtension(system).system.provider.getDefaultAddress

  implicit val materializer = ActorMaterializer()
  // needed for the future flatMap/onComplete in the end
  implicit val executionContext = system.dispatcher

  val route =
    pathPrefix("hello") {
      get {
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http</h1>"))
      }
    } ~ path(Segment / Segment) { (key, value) =>
      complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"<h1>key: $key, value: $value"))
    }

  import java.net.ServerSocket

  val bindingFuture = Http().bindAndHandle(route, "localhost", 0)

  println(s"Actor system: start: $defaultAddress, port: ${defaultAddress.port}")
  bindingFuture.foreach(sb => println(s"Server online at http://localhost:${sb.localAddress}/\nPress RETURN to stop..."))

  StdIn.readLine() // let it run until user presses return
  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => system.terminate()) // and shutdown when done

}
