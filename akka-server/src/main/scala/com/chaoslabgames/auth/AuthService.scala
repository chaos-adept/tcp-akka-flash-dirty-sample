package com.chaoslabgames.auth

import akka.actor.{ActorRef, Actor, ActorLogging}
import com.chaoslabgames.core.TaskService.CommandTask
import com.chaoslabgames.core.user.User
import com.chaoslabgames.core._
import com.chaoslabgames.session.Session

import scala.collection.mutable.ListBuffer

/**
 * @author <a href="mailto:denis.rykovanov@gmail.com">Denis Rykovanov</a>
 *         on 06.09.2015.
 */
class AuthService extends Actor with ActorLogging {

  var idCounter:Long = 0l
  val registeredUsers = new ListBuffer[User]

  override def receive = {
    case CommandTask(session, cmd) => cmd match {
      case authCmd: AuthCmd => auth(session, authCmd.data)
      case registerCmd: RegisterCmd => register(session, registerCmd.data)
    }
    case _ => log.info("unknown command")
  }

  def auth(session: ActorRef, authReqData: AuthReqData): Unit = {
    val result = registeredUsers.find( user => (user.name == authReqData.name) && (user.password == authReqData.password))
    if (result.isEmpty) {
      session ! AuthErrEvent(AuthFailedData(1))
    } else {
      session ! AuthEvent(AuthRespData(result.get.id))
    }

  }

  def register(session: ActorRef, data: AuthReqData): Unit = {
    idCounter += 1
    val user = new User(idCounter, data.name, data.password)
    registeredUsers += user
    session ! AuthEvent(AuthRespData(user.id))
  }
}

