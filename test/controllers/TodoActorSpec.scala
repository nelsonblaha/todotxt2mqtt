package controllers

import actors.TodoActor
import actors.TodoActor.{Add, HideContext, ListGet, ShowContext, TodoItem}
import akka.actor.ActorSystem
import akka.testkit.{TestKit, TestProbe}
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.duration._

class TodoActorSpec extends TestKit(ActorSystem("TodoActorSpec")) with AnyWordSpecLike {

  val testTodoLines = List(
    "(A) foo @home",
    "(B) bar",
    "(C) bazz @mitchell"
  )

  def setup = {
    val messageProbe = TestProbe()(system)

    val todoActor = system.actorOf(TodoActor.props("/dev/null", Some(testTodoLines)))

    (messageProbe, todoActor)
  }

  "Loading todo list" should {
    val (messageProbe, todoActor) = setup

    "write the list back" in {
      todoActor.tell(ListGet, messageProbe.ref)

      messageProbe.fishForSpecificMessage(1 second) {
        case string if string == testTodoLines.sorted.mkString("\n") => true
      }
    }
  }

  "Adding todo item" should {
    val (messageProbe, todoActor) = setup

    todoActor.tell(Add(TodoItem("(D) added")), messageProbe.ref)

    "add the item" in {
      todoActor.tell(ListGet, messageProbe.ref)

      messageProbe.fishForSpecificMessage(1 second) {
        case string if string == (testTodoLines :+ "(D) added").mkString("\n") => true
      }
    }
  }

  "Changing contexts" should {

    val (messageProbe, todoActor) = setup

    "hide context" in {
      todoActor ! HideContext("home")
      todoActor.tell(ListGet, messageProbe.ref)
      messageProbe.fishForSpecificMessage(1 seconds) {
        case "(B) bar\n(C) bazz @mitchell" => true
      }
    }

    "show context" in {
      todoActor ! ShowContext("home")
      todoActor.tell(ListGet, messageProbe.ref)
      messageProbe.fishForSpecificMessage(1 seconds) {
        case string if string == testTodoLines.sorted.mkString("\n") => true
      }
    }
  }
}
