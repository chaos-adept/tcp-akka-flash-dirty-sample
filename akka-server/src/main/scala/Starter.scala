import akka.actor.{ActorSystem, Props}
import com.chaoslabgames.auth.AuthService
import com.chaoslabgames.core.{GameModelService, TaskService}
import com.chaoslabgames.tcpfront.AkkaNetServerTCP

/**
 * @author <a href="mailto:denis.rykovanov@gmail.com">Denis Rykovanov</a>
 *         on 05.09.2015.
 */
object Starter extends App {


  val actorSystem = ActorSystem("akka-server")

  val actorTasks  = actorSystem.actorOf(Props[TaskService], "task")

  val authService  = actorSystem.actorOf(Props[AuthService], "auth")

  val gameModelService  = actorSystem.actorOf(Props[GameModelService], "gameModel")

  val actorNet    = actorSystem.actorOf(AkkaNetServerTCP.props("127.0.0.1", 8899, actorTasks), "tcp-front")
}
