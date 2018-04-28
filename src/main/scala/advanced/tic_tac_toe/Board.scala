package advanced.tic_tac_toe

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import Stream._

object Board {

  sealed trait BoardStatus
  object InGame           extends BoardStatus
  object Win              extends BoardStatus
  object Tie              extends BoardStatus
  object BoardInvalidMove extends BoardStatus
}

class Board {
  import Board._

  val board: mutable.Seq[ArrayBuffer[Int]] =
    ArrayBuffer(
      ArrayBuffer(0, 0, 0),
      ArrayBuffer(0, 0, 0),
      ArrayBuffer(0, 0, 0)
    )

  val X = 1
  val O = 2

  def move(row: Int, column: Int, isX: Boolean): BoardStatus = {
    def move(row: Int, column: Int): BoardStatus = {
      if (board(row)(column) == 0) {
        val newValue = if (isX) X else O
        board(row)(column) = newValue

        val tmp: Seq[Boolean] =
          checkRow(row, newValue) #:: checkColumn(column, newValue) #:: checkDiagonal(newValue) #:: empty[Boolean]

        if (tmp.exists(x => x))
          Win
        else if (inGame()) InGame
        else Tie
      } else BoardInvalidMove
    }
    move(row - 1, column - 1)
  }

  private def inGame(): Boolean = board.flatten.contains(0)

  private def checkRow(row: Int, newValue: Int): Boolean =
    board(row).forall(_ == newValue)

  private def checkColumn(column: Int, newValue: Int): Boolean =
    board.map(_(column)).forall(_ == newValue)

  private def checkDiagonal(newValue: Int): Boolean =
    List(board(0)(0), board(1)(1), board(2)(2)).forall(_ == newValue) ||
      List(board(2)(0), board(1)(1), board(0)(2)).forall(_ == newValue)

}
