package com.chaoslabgames.session

import akka.actor._
import com.chaoslabgames.core.TaskService.CommandTask
import com.chaoslabgames.core._
import com.chaoslabgames.core.datavalue.DataValue.{RoomSessionInfo, AuthSessionInfo}
import com.chaoslabgames.session.Session._

/**
 * @author <a href="mailto:denis.rykovanov@gmail.com">Denis Rykovanov</a>
 *         on 06.09.2015.
 */
class Session(val id: Long, val connection: ActorRef, val taskService: ActorRef) extends Actor with FSM[State, Data] with ActorLogging {

  startWith(UnAuthState, Unauthorized)

  when(UnAuthState) {
    case Event(msg, _) =>
      msg match {
        case auth:AuthEvent =>
          log.info("session is authenticated for userId: {}", auth.data.id)
          forwardMessage(msg)
          goto(AuthState) using new Authorized(AuthSessionInfo(auth.data.id, self)) replying (msg)
        case _ =>
          forwardMessage(msg)
          stay()
      }
  }

  when(AuthState) {
    case Event(cmd:CreateRoomCmd, authData:Authorized) =>
      taskService ! TaskService.CreateRoomTask(authData.session, cmd.data.name)
      stay()
    case Event(cmd:JoinCmd, authData:Authorized) =>
      taskService ! TaskService.JoinTask(RoomSessionInfo(cmd.roomId, authData.session))
      stay()
    case Event(cmd:LeaveCmd, authData:InRoomData) =>
      taskService ! TaskService.LeaveTask(RoomSessionInfo(authData.roomId, authData.session))
      stay()
    case Event(je:JoinEvent, authData:Authorized) =>
      forwardMessage(je)
      stay using new InRoomData(authData.session, je.roomId)
    case Event(AuthCmd | RegisterCmd, authData:Authorized) =>
      connection ! AuthRequiredEvent(AuthFailedData(3))
      stay()
  }

  whenUnhandled {
    case Event(event, _) =>
      forwardMessage(event)
      stay()
  }

  def forwardMessage(msg:Any) = {
    msg match {
      case m: Cmd => m.msg.stereotype match {
        case Cmd.TYPE_CMD =>
          if (m.msg.authRequired && !stateData.authorized) {
            log.info("try to exec authrequired command in unauth state")
            connection ! AuthRequiredEvent(AuthFailedData(2))
          }
          else {
            log.info("send cmd as task - {}", m)
            taskService ! CommandTask(self, m)
          }
        case Cmd.TYPE_EVENT =>
          log.info("send event to connection - {}", m)
          connection ! m
        case _ => log.info("unknown message stereotype {}")
      }
      case unknown => log.info("unknown message type {}", unknown)
    }
  }

  initialize()

}

object Session {
  def props(id: Long, connection: ActorRef, taskService: ActorRef) = Props(
    new Session(id, connection, taskService)
  )


  sealed trait State

  case object UnAuthState extends State

  case object AuthState extends State

  sealed trait Data { def authorized = false }

  case object Unauthorized extends Data

  class Authorized(val session: AuthSessionInfo) extends Data { override val authorized = true }
  class InRoomData(override val session: AuthSessionInfo, val roomId:Long) extends Authorized(session)

}
