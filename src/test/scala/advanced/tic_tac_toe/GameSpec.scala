package advanced.tic_tac_toe

import advanced.tic_tac_toe.Game.{Move, Status}
import akka.actor.testkit.typed.scaladsl.{BehaviorTestKit, TestInbox}
import akka.actor.typed._
import akka.actor.typed.scaladsl._
import org.scalatest.{Matchers, WordSpecLike}

class GameSynchronousSpec extends WordSpecLike with Matchers {
  //TODO: There is a lot of repetition in those test. How to make it better?

  "A Game behavior " must {
    val player1: TestInbox[Status] = TestInbox[Status]("player1")
    val player2: TestInbox[Status] = TestInbox[Status]("player2")

    "be able to start game" in {
      BehaviorTestKit(Game.startGame(player1.ref, player2.ref))

      player1.expectMessage(Game.YourMove)
    }

    "allow player1 to make a first move" in {
      val testKit = BehaviorTestKit(Game.startGame(player1.ref, player2.ref))
      player1.receiveMessage()

      testKit.run(Move(1, 1, player1.ref))
      player1.expectMessage(Game.Accepted)
      player2.expectMessage(Game.YourMove)
    }

    "not allow player2 to make a first move" in {
      val testKit = BehaviorTestKit(Game.startGame(player1.ref, player2.ref))

      testKit.run(Move(1, 1, player2.ref))
      player2.expectMessage(Game.NotYourMove)
    }

    "allow player2 to make a move after player1" in {
      val testKit = BehaviorTestKit(Game.startGame(player1.ref, player2.ref))

      testKit.run(Move(1, 1, player1.ref))
      player2.expectMessage(Game.YourMove)

      testKit.run(Move(2, 2, player2.ref))
      player2.expectMessage(Game.Accepted)
    }

    "tell player2 that move was invalid" in {
      val testKit = BehaviorTestKit(Game.startGame(player1.ref, player2.ref))

      testKit.run(Move(1, 1, player1.ref))
      player2.receiveMessage()

      testKit.run(Move(1, 1, player2.ref))
      player2.expectMessage(Game.InvalidMove)
    }

    "communicate a win of a player" in {
      val testKit = BehaviorTestKit(Game.startGame(player1.ref, player2.ref))

      testKit.run(Move(1, 1, player1.ref))
      testKit.run(Move(1, 2, player2.ref))

      testKit.run(Move(2, 2, player1.ref))
      testKit.run(Move(1, 3, player2.ref))

      player1.receiveAll()
      player2.receiveAll()
      testKit.run(Move(3, 3, player1.ref))

      player1.expectMessage(Game.YouWin)
      player2.expectMessage(Game.YouLoose)

      testKit.run(Move(2, 2, player2.ref))
      testKit.run(Move(2, 2, player1.ref))

      player1.expectMessage(Game.TheEnd("The winner is: akka.actor.typed.inbox://anonymous/player1"))
      player2.expectMessage(Game.TheEnd("The winner is: akka.actor.typed.inbox://anonymous/player1"))
    }

    "communicate a tie" in {
      val testKit = BehaviorTestKit(Game.startGame(player1.ref, player2.ref))

      val player1Moves = List(
      Move(1, 1, player1.ref),
      Move(1, 3, player1.ref),
      Move(2, 1, player1.ref),
      Move(2, 2, player1.ref)
      )

      val player2Moves = List(
      Move(1, 2, player2.ref),
      Move(2, 3, player2.ref),
      Move(3, 1, player2.ref),
      Move(3, 3, player2.ref)
      )

      (player1Moves zip player2Moves).foreach{
        case (one, two) =>
          testKit.run(one)
          testKit.run(two)
      }

      player1.receiveAll()
      player2.receiveAll()

      testKit.run(Move(3, 2, player1.ref))
      player1.expectMessage(Game.Tie)
      player2.expectMessage(Game.Tie)

      testKit.run(Move(2, 2, player2.ref))
      testKit.run(Move(2, 2, player1.ref))
      player1.expectMessage(Game.TheEnd("It is a tie"))
      player2.expectMessage(Game.TheEnd("It is a tie"))
    }
  }

}
