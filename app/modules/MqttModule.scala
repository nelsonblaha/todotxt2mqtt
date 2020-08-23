package modules

import actors.{MqttActor, TodoActor}
import javax.inject._
import play.api.libs.concurrent.AkkaGuiceSupport
import akka.actor.{ActorRef, ActorSystem}

import scala.concurrent.duration._
import com.google.inject.AbstractModule
import com.typesafe.config.Config
import play.api.{Configuration, Environment}

@Singleton
class MqttModuleStarter @Inject()(system: ActorSystem,
                                  @Named("MqttActor") mqttActor: ActorRef,
                                  @Named("TodoActor") todoActor: ActorRef) {
  implicit val ec = scala.concurrent.ExecutionContext.global

  system.scheduler.scheduleOnce(1.seconds, mqttActor, todoActor)
  system.scheduler.scheduleOnce(1.seconds, todoActor, mqttActor)
}

class MqttModule @Inject()(environment: Environment, configuration: Configuration) extends AbstractModule with AkkaGuiceSupport {
  val config = configuration.underlying
  override def configure() = {
    val mqttProps = MqttActor.props(config.getString("mqtt.host"), config.getString("mqtt.user"), config.getString("mqtt.pass"))
    bindActor[MqttActor]("MqttActor", _ => mqttProps)

    val todoProps = TodoActor.props(config.getString("todo.root"), None)
    bindActor[TodoActor]("TodoActor", _ => todoProps)

    bind(classOf[MqttModuleStarter]).asEagerSingleton
  }
}