package actors

import java.io.FileWriter

import actors.MqttActor.{CheckMessages, Publish, connectionSettings}
import actors.TodoActor.Add
import actors.TodoActor.TodoItem.priorities
import akka.Done
import akka.actor._
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.util.ByteString
import com.typesafe.config.Config

import scala.concurrent.Future

class TodoActor extends Actor with ActorLogging {
  implicit val actorSystem: ActorSystem = ActorSystem()

  var mqttActor: ActorRef = null
  val todoFile = "/todo/todo.txt"
  var lines = List.empty[String]

  def readTodo: Unit = {
    val source = scala.io.Source.fromFile(todoFile)
    lines = source.getLines.toList
    source.close
  }

  override def preStart() = readTodo

  def receive = {
    case mqtt: ActorRef => mqttActor = mqtt
    case Add(item) =>
      // TODO doesn't handle empty TODO file correctly
      val newLine = if(lines.nonEmpty && lines.reverse.headOption.exists(_.length == 0)) { "" } else { "\n" }
      val fw = new FileWriter(todoFile, true)
      try {
        fw.write(newLine+item.toLine)
      }
      finally fw.close()
  }
}

object TodoActor {
  case class TodoItem(priority: Option[Int] = None,
                      body: String) {
    def toLine: String = {
      val priorityString = priority map { p =>
        s"(${priorities(p)}) "
      } getOrElse ""
      priorityString + body
    }
  }

  object TodoItem {
    def apply(string: String): TodoItem = {
      val (priority, body): (Option[Int], String) = "^\\([A-Z]\\)".r.findAllIn(string).toList.headOption map { p =>
        priorities.indexOf(p.charAt(1).toString)
      } match {
        case pri @ Some(p) if p > -1 && p < 24 =>
          // TODO: handle missing space after priority
          (pri, string.drop(4))
        case _ =>
          (None, string)
      }
      TodoItem(priority, body)
    }
    val priorities = Seq("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "W", "X", "Y", "Z")
  }

  case class Add(item: TodoItem)

  def props(config: Config) = Props(new MqttActor(config))
}