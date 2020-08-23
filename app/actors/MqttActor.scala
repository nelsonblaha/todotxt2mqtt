package actors

import actors.MqttActor.{CheckMessages, Publish, connectionSettings}
import actors.TodoActor.{Add, TodoItem}
import akka.Done
import akka.actor._
import akka.stream.alpakka.mqtt.scaladsl.{MqttSink, MqttSource}
import akka.stream.alpakka.mqtt.{MqttConnectionSettings, MqttMessage, MqttQoS, MqttSubscriptions}
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.util.ByteString
import com.typesafe.config.Config
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

import scala.concurrent.Future
import javax.inject._

class MqttActor @Inject() (config: Config) extends Actor with ActorLogging {

  val mqttSource: Source[MqttMessage, Future[Done]] =
    MqttSource.atMostOnce(
      connectionSettings(config).withClientId(clientId = "source-spec/source"),
      MqttSubscriptions(Map("todo/add" -> MqttQoS.AtLeastOnce)),
      bufferSize = 8
    )

  implicit val actorSystem: ActorSystem = ActorSystem()

  var todoActor: ActorRef = null

  override def preStart() = self ! CheckMessages

  def receive = {
    case todo: ActorRef => todoActor = todo
    case Publish(payload, topic) =>
      val sink: Sink[MqttMessage, Future[Done]] = MqttSink(connectionSettings(config), MqttQoS.AtLeastOnce)
      Source(MqttMessage(topic, payload) :: Nil).runWith(sink)
    case CheckMessages =>
      val (subscribed, streamResult) = mqttSource
        .take(1)
        .toMat(Sink.seq)(Keep.both)
        .run()

      implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

      streamResult map { sequence =>
        sequence map { message =>
          println(s"----saw message ${message.topic} ${message.payload}")
          message.topic match {
            case "todo/add" =>
              todoActor ! Add(TodoItem(message.payload.utf8String))
          }
        }
      }

      Thread.sleep(5000)
      self ! CheckMessages
  }
}

object MqttActor {
  def connectionSettings(config: Config) = MqttConnectionSettings(
    config.getString("mqtt.host"),
    "todotxt2mqtt",
    new MemoryPersistence
  ).withAuth(config.getString("mqtt.user"), config.getString("mqtt.pass"))

  case object CheckMessages

  case class Publish(payload: ByteString,
                     topic: String = "todo")

  def props(config: Config) = Props(new MqttActor(config))
}