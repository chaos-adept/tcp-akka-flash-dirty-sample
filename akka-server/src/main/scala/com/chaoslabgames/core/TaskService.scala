package com.chaoslabgames.core

import akka.actor.{Actor, ActorLogging, ActorRef}
import com.chaoslabgames.packet.{LoginResp, PacketMSG}
import com.chaoslabgames.session.Session

/**
 * @author <a href="mailto:denis.rykovanov@gmail.com">Denis Rykovanov</a>
 *         on 05.09.2015.
 */
class TaskService extends Actor with ActorLogging {
  import TaskService._

  // -----
//  val authService = context.actorSelection("akka://server/user/auth")
//  val gameService = context.actorSelection("akka://server/user/game")

  // ----- actor -----
  override def preStart() {
    log.info("Starting task service")
  }

  override def receive = {
    case task: CommandTask => handlePacket(task)

    case _ => log.info("unknown message")
  }

  override def postStop() {
    // clean up resources
    log.info("Stoping task service")
  }


  // ----- handles -----
  def handlePacket(task: CommandTask) = {
    task.comm.getCmd match {
      case Cmd.Auth.code =>
        //authService ! AuthService.Authenticate(task.session, task.comm)
        val login:LoginResp.Builder = LoginResp.newBuilder()
        login.setId(123)
        task.session ! Session.Send(Cmd.AuthResp, login.build().toByteArray)
      case Cmd.Join.code =>
        //gameService ! GmService.JoinGame(task.session)
      case Cmd.Move.code =>
        //gameService ! Room.PlayerMove(task.session, task.comm)
      case _ => log.info("Crazy message")
    }
  }
}

object TaskService {

  // ----- API -----
  case class CommandTask(session: ActorRef, comm: PacketMSG)
}
