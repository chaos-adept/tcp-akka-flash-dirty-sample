package com.chaoslabgames.core

import akka.actor.{Actor, ActorLogging, ActorRef}
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
  //  val gameService = context.actorSelection("akka://server/user/game")

  var idCounter: Long = 0l
  val rooms = new ListBuffer[RoomData]

  // ----- actor -----
  override def preStart() {
    log.info("Starting task service")
  }

  override def receive = {
    case task: CommandTask => handlePacket(task)
    case cr: CreateRoomTask =>
      idCounter += 1
      val room = RoomData(cr.roomName, idCounter, cr.userId)
      rooms += room
      cr.session ! RoomCreatedEvent(room)
    case jt:JoinTask =>
      jt.session ! JoinEvent(jt.userId, jt.roomId)
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
      case GetRoomListCmd =>
        task.session ! RoomListEvent(RoomListData(rooms.toSet))
      //case Cmd.Join.code =>
      //gameService ! GmService.JoinGame(task.session)
      //case Cmd.Move.code =>
      //gameService ! Room.PlayerMove(task.session, task.comm)
      case _ => log.info("Crazy message")
    }
  }
}

object TaskService {

  // ----- API -----
  case class CommandTask(session: ActorRef, cmd: Cmd)

  case class CreateRoomTask(session: ActorRef, userId: Long, roomName: String)

  case class JoinTask(session: ActorRef, roomId:Long, userId:Long)

}
