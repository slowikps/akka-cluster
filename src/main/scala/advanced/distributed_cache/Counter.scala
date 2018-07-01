package advanced.distributed_cache

import akka.cluster.ddata.GCounter
import akka.cluster.ddata.GCounterKey
import akka.cluster.ddata.ReplicatedData
import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter._
import akka.cluster.Cluster
import akka.cluster.ddata.typed.scaladsl.{DistributedData, Replicator}

object Counter {
  sealed trait ClientCommand
  final case object Increment extends ClientCommand
  final case class GetValue(replyTo: ActorRef[Int]) extends ClientCommand

  private sealed trait InternalMsg extends ClientCommand
  private case class InternalUpdateResponse[A <: ReplicatedData](rsp: Replicator.UpdateResponse[A]) extends InternalMsg
  private case class InternalGetResponse[A <: ReplicatedData](rsp: Replicator.GetResponse[A]) extends InternalMsg

  val Key = GCounterKey("counter")

  def behavior: Behavior[ClientCommand] =
    Behaviors.setup[ClientCommand] { ctx ⇒
      // The ddata types still need the implicit untyped Cluster.
      // We will look into another solution for that.
      implicit val cluster: Cluster = akka.cluster.Cluster(ctx.system.toUntyped)
      val replicator: ActorRef[Replicator.Command] = DistributedData(ctx.system).replicator

      // use message adapters to map the external messages (replies) to the message types
      // that this actor can handle (see InternalMsg)
      val updateResponseAdapter: ActorRef[Replicator.UpdateResponse[GCounter]] =
        ctx.messageAdapter(InternalUpdateResponse.apply)

      val getResponseAdapter: ActorRef[Replicator.GetResponse[GCounter]] =
        ctx.messageAdapter(InternalGetResponse.apply)

      Behaviors.receive[ClientCommand] { (_, msg) ⇒
        msg match {
          case Increment ⇒
            replicator ! Replicator.Update(Key, GCounter.empty, Replicator.WriteLocal, updateResponseAdapter)(_ + 1)
            Behaviors.same

          case GetValue(replyTo) ⇒
            replicator ! Replicator.Get(Key, Replicator.ReadLocal, getResponseAdapter, Some(replyTo))
            Behaviors.same

          case internal: InternalMsg ⇒ internal match {
            case InternalUpdateResponse(_) ⇒ Behaviors.same // ok

            case InternalGetResponse(rsp @ Replicator.GetSuccess(Key, Some(replyTo: ActorRef[Int] @unchecked))) ⇒
              val value = rsp.get(Key).value.toInt
              replyTo ! value
              Behaviors.same

            case InternalGetResponse(rsp) ⇒
              Behaviors.unhandled // not dealing with failures
          }
        }
      }
    }

}