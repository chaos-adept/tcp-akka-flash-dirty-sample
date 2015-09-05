import akka.actor.{ActorSystem, Props}
import com.chaoslabgames.core.TaskService
import com.chaoslabgames.tcpfront.AkkaNetServerTCP

/**
 * @author <a href="mailto:denis.rykovanov@gmail.com">Denis Rykovanov</a>
 *         on 05.09.2015.
 */
object Starter extends App {


  val actorSystem = ActorSystem("akka-server")

  val actorTasks  = actorSystem.actorOf(Props[TaskService], "task")

  val actorNet    = actorSystem.actorOf(AkkaNetServerTCP.props("127.0.0.1", 8889), "tcp-front")
}
