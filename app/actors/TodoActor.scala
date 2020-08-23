package actors

import java.io.{File, FileWriter}
import actors.TodoActor.{Add, HideContext, ShowContext, TodoItem}
import actors.TodoActor.TodoItem.priorities
import akka.actor._
import actors.TodoActor.ListGet

class TodoActor(todoRoot: String, testLines: Option[List[String]] = None) extends Actor with ActorLogging {
  implicit val actorSystem: ActorSystem = ActorSystem()

  var mqttActor: ActorRef = null
  val todoFile = todoRoot + "/todo.txt"
  val hiddenFile = todoRoot + "/hidden.txt"
  var todo = List.empty[TodoItem]
  var hidden4tests = List.empty[TodoItem]

  override def preStart() = readTodo

  def receive = {
    case mqtt: ActorRef => mqttActor = mqtt

    case Add(item) => add(todo, item)

    case ListGet =>
      readTodo
      sender ! printList(todo)

    case ShowContext(context: String) =>
      readTodo
      val hidden = testLines.map { _ =>
        hidden4tests
      } getOrElse {
        val source = scala.io.Source.fromFile(hiddenFile)
        val hiddenItems = source.getLines.toList.map(TodoItem(_))
        source.close
        hiddenItems
      }

      new File(hiddenFile).delete

      hidden.map {
        case line if line.contexts.contains(context) =>
          if(testLines.isEmpty) add(todo, line, todoFile)
          todo = (todo :+ line).reverse
        case line if !line.contexts.contains(context) =>
          if(testLines.isEmpty) add(readList(hiddenFile), line, hiddenFile)
      }

    case HideContext(context: String) =>
      readTodo

      new File(todoFile).delete

      var newTodo4Tests = List.empty[TodoItem]

      todo.foreach {
        case line if !line.contexts.contains(context) =>
          if(testLines.isEmpty) add(todo, line, todoFile) else {
            newTodo4Tests = newTodo4Tests :+ line
          }
          todo = todo :+ line
        case line if line.contexts.contains(context) =>
          if(testLines.nonEmpty) {
            hidden4tests = hidden4tests :+ line
          } else {
            add(readList(hiddenFile), line, hiddenFile)
          }
      }

      if(newTodo4Tests.nonEmpty) {
        todo = newTodo4Tests
      }
  }

  def readList(path: String): List[TodoItem] = {
    testLines map { lines =>
      lines.map(TodoItem(_))
    } getOrElse {
      val source = scala.io.Source.fromFile(path)
      val lines = source.getLines.toList.reverse.filterNot(_.isEmpty).map { l =>
        TodoItem(l)
      }
      source.close
      lines
    }
  }

  def readTodo: Unit = {
    if(todo.isEmpty) todo = readList(todoFile)
  }

  def printList(items: List[TodoItem]): String = items.sortBy(_.raw).map(_.toLine).mkString("", "\n", "")

  def add(list: List[TodoItem], item: TodoItem, path: String = todoFile) = {
    readTodo
    write(path, printList(list :+ item))
  }

  def write(path: String = todoFile, print: String = printList(todo)): Unit = {
    new File(path).delete
    val fw = new FileWriter(path, true)
    try {
      fw.write(print)
    }
    finally fw.close()
  }
}

object TodoActor {
  case class TodoItem(raw: String,
                      priority: Option[Int] = None,
                      body: String,
                      contexts: List[String]) {
    def toLine: String = {
      val priorityString = priority map { p =>
        s"(${priorities(p)}) "
      } getOrElse ""
      priorityString + body
    }
  }

  object TodoItem {
    def apply(string: String): TodoItem = {
      // TODO: remove contexts from body and re-add them in TodoItem.toLine
      val (priority, body): (Option[Int], String) = "^\\([A-Z]\\)".r.findAllIn(string).toList.headOption map { p =>
        priorities.indexOf(p.charAt(1).toString)
      } match {
        case pri @ Some(p) if p > -1 && p < 24 =>
          // TODO: handle missing space after priority
          (pri, string.drop(4))
        case _ =>
          (None, string)
      }

      val contexts = " @([\\S]*)".r.findAllIn(string).toList.map(_.drop(2))

      TodoItem(string, priority, body, contexts)
    }
    val priorities = Seq("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "W", "X", "Y", "Z")
  }

  case object ListGet
  case class Add(item: TodoItem)
  case class ShowContext(context: String)
  case class HideContext(context: String)

  def props(todoRoot: String, testLines: Option[List[String]]) = Props(new TodoActor(todoRoot, testLines))
}