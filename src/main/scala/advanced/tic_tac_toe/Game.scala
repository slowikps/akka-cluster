package advanced.tic_tac_toe

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}

object Game {

  case class Move(row: Int, column: Int, sender: ActorRef[Status]) {
    require(row > 0 && row < 4, s"Invalid move for: row=$row")
    require(column > 0 && column < 4, s"Invalid move for: column=$column")
  }

  sealed trait Status
  object YourMove                    extends Status
  object NotYourMove                 extends Status
  object InvalidMove                 extends Status
  object Accepted                    extends Status
  object YouWin                      extends Status
  object YouLoose                    extends Status
  object Tie                         extends Status
  case class TheEnd(message: String) extends Status

  def startGame(player1: ActorRef[Status], player2: ActorRef[Status]): Behaviors.Receive[Move] = {
    def game(board: Board, activePlayer: ActorRef[Status], nextPlayer: ActorRef[Status]): Behaviors.Receive[Move] = {
      Behaviors.receive { (_, msg) =>
        if (msg.sender == activePlayer) {
          board.move(msg.row, msg.column, activePlayer == player1) match {
            case Board.BoardInvalidMove =>
              activePlayer ! InvalidMove
              Behavior.same
            case Board.InGame =>
              activePlayer ! Accepted
              nextPlayer ! YourMove
              game(board, nextPlayer, activePlayer)
            case Board.Tie =>
              activePlayer ! Tie
              nextPlayer ! Tie
              gameEnded(TheEnd("It is a tie"))
            case Board.Win =>
              activePlayer ! YouWin
              nextPlayer ! YouLoose
              gameEnded(TheEnd(s"The winner is: ${activePlayer.path}"))
          }
        } else {
          msg.sender ! NotYourMove
          Behavior.same
        }
      }
    }

    player1 ! YourMove
    game(new Board(), player1, player2)
  }

  private def gameEnded(status: TheEnd): Behaviors.Receive[Move] =
    Behaviors.receive { (_, msg) =>
      msg.sender ! status
      Behavior.same
    }
}
