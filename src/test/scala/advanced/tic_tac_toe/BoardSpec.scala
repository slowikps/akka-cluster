package advanced.tic_tac_toe

import org.scalatest.{Matchers, WordSpecLike}

class BoardSpec extends WordSpecLike with Matchers {

  "A Board " must {
    "be printed nicely" in {
      val sut = new Board()
      sut.move(row = 1, column = 1, isX = true)
      sut.move(row = 2, column = 3, isX = true)
      sut.move(row = 3, column = 2, isX = true)

      sut.move(row = 1, column = 3, isX = false)
      sut.move(row = 2, column = 1, isX = false)
      sut.move(row = 3, column = 1, isX = false)

      sut.toString should ===(
        """|X| |O
           |-----
           |O| |X
           |-----
           |O|X| """.stripMargin
      )
    }
  }

}
