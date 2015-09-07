package com.chaoslabgames.session

import akka.actor._
import com.chaoslabgames.core.TaskService.CommandTask
import com.chaoslabgames.core._

import scala.collection.parallel.immutable
import com.chaoslabgames.session.Session._

/**
 * @author <a href="mailto:denis.rykovanov@gmail.com">Denis Rykovanov</a>
 *         on 06.09.2015.
 */
class Session(val id: Long, val connection: ActorRef, val taskService: ActorRef) extends Actor with FSM[State, Data] with ActorLogging {

  startWith(UnAuthState, Unauthorized)

  when(UnAuthState) {
    case Event(msg, _) => {
      msg match {
        case auth:AuthEvent =>
          log.info("sessin is authenticated for userId: {}", auth.data.id)
          forwardMessage(msg)
          goto(AuthState) using Authorized(auth.data.id) replying (msg)
        case _ =>
          forwardMessage(msg)
          stay()
      }
    }
  }

  when(AuthState) {
    case Event(cmd:CreateRoomCmd, authData:Authorized) =>
      taskService ! TaskService.CreateRoomTask(self, authData.userId, cmd.data.name)
      stay()
    case Event(cmd:JoinCmd, authData:Authorized) =>
      taskService ! TaskService.JoinTask(self, cmd.roomId, authData.userId)
      stay()
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
            log.info("send cmd as task {}", m)
            taskService ! CommandTask(self, m)
          }
        case Cmd.TYPE_EVENT =>
          log.info("send event to connection {}", m)
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

  case class Authorized(userId: Long) extends Data { override val authorized = true }

}
