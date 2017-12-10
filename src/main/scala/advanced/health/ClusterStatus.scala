package advanced.health

import advanced.health.ClusterStatus.PrintStatus
import akka.actor.{Actor, ActorSystem, PoisonPill, Props}
import akka.cluster.singleton.{ClusterSingletonManager, ClusterSingletonManagerSettings, ClusterSingletonProxy, ClusterSingletonProxySettings}

object ClusterStatus {
  private val name = "cluster-status-singleton"
  private val managerName = s"$name-manager"
  private val proxyName = s"$name-proxy"

  private val managerPath = s"/user/$managerName"

  def props: Props =
    Props(new ClusterStatus)

  def createSingletonManager(system: ActorSystem, props: Props = props) =
    system.actorOf(
      ClusterSingletonManager.props(
        props,
        PoisonPill,
        ClusterSingletonManagerSettings(system)), //.withRole("not-everywhere-restriction")),
      managerName
    )

  def createSingletonProxy(system: ActorSystem, props: Props = props) =
    system.actorOf(
      ClusterSingletonProxy.props(
        managerPath,
        ClusterSingletonProxySettings(system)),
      proxyName
    )

  case object PrintStatus
}

class ClusterStatus extends Actor {

  override def receive = {
    case PrintStatus => println("About to print status")
  }
}
