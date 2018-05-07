package advanced.supervision

import akka.actor.typed.SupervisorStrategy
import akka.actor.typed.scaladsl.Behaviors
import scala.concurrent.duration._
//https://doc.akka.io/docs/akka/current/typed/fault-tolerance.html
object AkkaTyped {

//  SupervisorStrategy.restartWithBackoff()
//  SupervisorStrategy.restartWithLimit()
  Behaviors
    .supervise(???)
    .onFailure[IllegalStateException](SupervisorStrategy.resume)

  Behaviors
    .supervise(???)
    .onFailure[IllegalStateException](
      SupervisorStrategy.restartWithLimit(
        maxNrOfRetries = 10,
        withinTimeRange = 10.seconds
      ))

  Behaviors
    .supervise(
      Behaviors
        .supervise(???)
        .onFailure[IllegalStateException](SupervisorStrategy.restart))
    .onFailure[IllegalArgumentException](SupervisorStrategy.stop)
}
