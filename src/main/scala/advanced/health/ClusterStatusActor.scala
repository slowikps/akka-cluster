package advanced.health

import advanced.health.ClusterStatusActor.PrintStatus
import akka.actor.{Actor, ActorSystem, Address, PoisonPill, Props}
import akka.cluster.ClusterEvent.{ClusterDomainEvent, InitialStateAsEvents, LeaderChanged, MemberUp}
import akka.cluster.singleton._
import akka.cluster.{Cluster, Member}

import scala.collection.mutable

case class ClusterStatus(leader: Option[Address], members: Set[Member])

object ClusterStatusActor {
  private val name        = "cluster-status-singleton"
  private val managerName = s"$name-manager"
  private val proxyName   = s"$name-proxy"

  private val managerPath = s"/user/$managerName"

  def props: Props =
    Props(new ClusterStatusActor)

  def createSingletonManager(system: ActorSystem, props: Props = props) =
    system.actorOf(
      ClusterSingletonManager
        .props(props, PoisonPill, ClusterSingletonManagerSettings(system)), //.withRole("not-everywhere-restriction")),
      managerName
    )

  def createSingletonProxy(system: ActorSystem, props: Props = props) =
    system.actorOf(
      ClusterSingletonProxy.props(managerPath, ClusterSingletonProxySettings(system)),
      proxyName
    )

  case object PrintStatus
}

class ClusterStatusActor extends Actor {

  val members                        = mutable.Set[Member]()
  var leaderAddress: Option[Address] = None

  override def preStart(): Unit = {
    super.preStart()
    Cluster(context.system)
      .subscribe(self, InitialStateAsEvents, classOf[ClusterDomainEvent])
  }

  override def postStop(): Unit = {
    Cluster(context.system).unsubscribe(self)
    super.postStop()
  }

  override def receive: PartialFunction[Any, Unit] = serviceMessageProcessor orElse clusterMessagesProcessor

  private def serviceMessageProcessor: PartialFunction[Any, Unit] = {
    case PrintStatus => sender() ! ClusterStatus(leaderAddress, members.toSet)
  }

  private def clusterMessagesProcessor: PartialFunction[Any, Unit] = {
    case MemberUp(m)      => members += m
    case LeaderChanged(a) => leaderAddress = a

//    case _ => ???  TODO: how this should be handled!!! Somehow globally, not sure how though
  }
}
