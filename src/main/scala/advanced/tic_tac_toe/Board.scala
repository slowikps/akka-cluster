package advanced.tic_tac_toe

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object Board {

  sealed trait BoardStatus
  object InGame           extends BoardStatus
  object Win              extends BoardStatus
  object Tie              extends BoardStatus
  object BoardInvalidMove extends BoardStatus
}

class Board {
  import Board._

  private val board: mutable.Seq[ArrayBuffer[Int]] =
    ArrayBuffer(
      ArrayBuffer(0, 0, 0),
      ArrayBuffer(0, 0, 0),
      ArrayBuffer(0, 0, 0)
    )

  private val Empty = 0
  private val X     = 1
  private val O     = 2

  def move(row: Int, column: Int, isX: Boolean): BoardStatus = {
    def move(row: Int, column: Int): BoardStatus = {
      if (board(row)(column) == Empty) {
        val newValue = if (isX) X else O
        board(row)(column) = newValue

        if (checkRow(row, newValue) || checkColumn(column, newValue) || checkDiagonal(newValue))
          Win
        else if (inGame()) InGame
        else Tie
      } else BoardInvalidMove
    }
    move(row - 1, column - 1)
  }

  private def inGame(): Boolean = board.flatten.contains(Empty)

  private def checkRow(row: Int, newValue: Int): Boolean =
    board(row).forall(_ == newValue)

  private def checkColumn(column: Int, newValue: Int): Boolean =
    board.map(_(column)).forall(_ == newValue)

  private def checkDiagonal(newValue: Int): Boolean =
    List(board(0)(0), board(1)(1), board(2)(2)).forall(_ == newValue) ||
      List(board(2)(0), board(1)(1), board(0)(2)).forall(_ == newValue)

  override def toString: String = {
    def printInt(in: Int) = in match {
      case Empty => " "
      case X     => "X"
      case O     => "O"
    }

    board.map(
      _.map(printInt).mkString("|")
    ).mkString("\n-----\n")
  }

}
