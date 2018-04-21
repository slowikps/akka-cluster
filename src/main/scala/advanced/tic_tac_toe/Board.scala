package advanced.tic_tac_toe

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

sealed trait BoardStatus
object InGame           extends BoardStatus
object Win              extends BoardStatus
object Tie              extends BoardStatus
object BoardInvalidMove extends BoardStatus

class Board {
  val board: mutable.Seq[ArrayBuffer[Int]] = ArrayBuffer(ArrayBuffer(0, 0, 0), ArrayBuffer(0, 0, 0), ArrayBuffer(0, 0, 0))

  def move(x: Int, y: Int, isX: Boolean): BoardStatus = {
    if (board(x - 1)(y - 1) == 0) {
      ???
    } else BoardInvalidMove
  }

}
