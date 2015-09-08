package com.chaoslabgames.core

import akka.actor.{Actor, ActorLogging, ActorRef}
import com.chaoslabgames.core.datavalue.DataValue.{RoomSessionInfo, AuthSessionInfo, RoomListData}
import com.chaoslabgames.core.user.User
import com.chaoslabgames.packet.{LoginResp, PacketMSG}
import com.chaoslabgames.session.Session

import scala.collection.mutable.ListBuffer

/**
 * @author <a href="mailto:denis.rykovanov@gmail.com">Denis Rykovanov</a>
 *         on 05.09.2015.
 */
class TaskService extends Actor with ActorLogging {

  import TaskService._

  // -----
  val authService = context.actorSelection("akka://akka-server/user/auth")
  val gameModelService = context.actorSelection("akka://akka-server/user/gameModel")
  //  val gameService = context.actorSelection("akka://server/user/game")

  // ----- actor -----
  override def preStart() {
    log.info("Starting task service")
  }

  override def receive = {
    case task: CommandTask => handlePacket(task)
    case CreateRoomTask(session, roomName) =>
      gameModelService ! GameModelService.CreateRoom(session, roomName)
    case JoinTask(session, roomId) =>
      //session ! JoinEvent(session.userId, roomId)
      gameModelService ! GameModelService.JoinRoom(session, roomId)
    case LeaveTask(session, roomId) =>
      //session ! LeaveEvent(session.userId, roomId)
      gameModelService ! GameModelService.LeaveRoom(session, roomId)
    case GetRoomListTask(session) =>
      gameModelService ! GameModelService.ListRoom(session)
    case _ => log.info("unknown message")
  }

  override def postStop() {
    // clean up resources
    log.info("Stoping task service")
  }


  // ----- handles -----
  def handlePacket(task: CommandTask) = {
    task.cmd match {
      case auth: AuthCmd => //fixme replace cmd by tasks
        //authService ! AuthService.Authenticate(task.session, task.comm)
        log.info("auth user name: {} / password: {}", auth.data.name, auth.data.password)
        authService ! task
      case register: RegisterCmd =>
        log.info("register user name: {} / password: {}", register.data.name, register.data.password)
        authService ! task
      case _ => log.info("Crazy message")
    }
  }
}

object TaskService {

  // ----- API -----
  case class CommandTask(session: ActorRef, cmd: Cmd)

  case class CreateRoomTask(session:AuthSessionInfo, roomName: String)

  case class JoinTask(session:AuthSessionInfo, roomId:Long)

  case class LeaveTask(session:AuthSessionInfo, roomId:Long)

  case class GetRoomListTask(session: AuthSessionInfo)

}
