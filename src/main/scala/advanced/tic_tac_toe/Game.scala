package advanced.tic_tac_toe

import akka.NotUsed
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, Terminated}

object Game {

  case class Move(x: Int, y: Int, sender: ActorRef[Status]) {
    require(x > 0 && x < 4, s"Invalid move for: x=$x")
    require(y > 0 && y < 4, s"Invalid move for: y=$y")
  }

  sealed trait Status
  object NotYourMove                 extends Status
  object InvalidMove                 extends Status
  object Accepted                    extends Status
  case class TheEnd(message: String) extends Status

  def startGame(player1: ActorRef[Status], player2: ActorRef[Status]): Behaviors.Receive[Move] = {
    def game(board: Board, activePlayer: ActorRef[Status], nextPlayer: ActorRef[Status]): Behaviors.Receive[Move] = {
      Behaviors.receive { (_, msg) =>
        if (msg.sender == activePlayer) {
          board.move(msg.x, msg.y, activePlayer == player1) match {
            case BoardInvalidMove =>
              activePlayer ! InvalidMove
              Behavior.same
            case InGame =>
              activePlayer ! Accepted
              game(board, nextPlayer, activePlayer)
            case Tie =>
              activePlayer ! TheEnd("It is a tie")
              nextPlayer ! TheEnd("It is a tie")
              gameEnded(TheEnd("It is a tie"))
            case Win =>
              activePlayer ! TheEnd(s"The winner is: $activePlayer")
              nextPlayer ! TheEnd(s"The winner is: $activePlayer")
              gameEnded(TheEnd(s"The winner is: $activePlayer"))
          }
        } else {
          msg.sender ! NotYourMove
          Behavior.same
        }
      }
    }

    game(new Board(), player1, player2)
  }

  private def gameEnded(status: TheEnd): Behaviors.Receive[Move] =
    Behaviors.receive { (_, msg) =>
      msg.sender ! status
      Behavior.same
    }
}
