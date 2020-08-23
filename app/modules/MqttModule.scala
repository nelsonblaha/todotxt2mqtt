package modules

import actors.{MqttActor, TodoActor}
import actors.MqttActor.Publish
import actors.TodoActor.{Add, TodoItem}
import javax.inject._
import play.api.libs.concurrent.AkkaGuiceSupport
import play.libs.Akka
import akka.actor.{ActorRef, ActorSystem}
import akka.util.ByteString

import scala.concurrent.duration._
import play.Application
import com.google.inject.AbstractModule

@Singleton
class MqttModuleStarter @Inject()(system: ActorSystem,
                                  @Named("MqttActor") mqttActor: ActorRef,
                                  @Named("TodoActor") todoActor: ActorRef) {
  implicit val ec = scala.concurrent.ExecutionContext.global

  system.scheduler.scheduleOnce(1.seconds, mqttActor, todoActor)
  system.scheduler.scheduleOnce(1.seconds, todoActor, mqttActor)
}

class MqttModule extends AbstractModule with AkkaGuiceSupport {
  override def configure() = {
    bindActor[MqttActor]("MqttActor")
    bindActor[TodoActor]("TodoActor")
    bind(classOf[MqttModuleStarter]).asEagerSingleton
  }
}