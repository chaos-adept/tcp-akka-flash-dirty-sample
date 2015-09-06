package com.chaoslabgames.session

import akka.actor._
import com.chaoslabgames.core.TaskService.CommandTask
import com.chaoslabgames.core.{TaskService, Cmd, ActorSelectors}

/**
 * @author <a href="mailto:denis.rykovanov@gmail.com">Denis Rykovanov</a>
 *         on 06.09.2015.
 */
class Session(val id:Long, val connection: ActorRef, val taskService: ActorRef) extends Actor with ActorLogging {

  override def receive = {
    case m:Cmd => m.msg.stereotype match {
      case Cmd.TYPE_CMD =>
        log.info("send cmd as task")
        taskService ! CommandTask(self, m)
      case Cmd.TYPE_EVENT =>
        log.info("send event to connection")
        connection ! m
      case _ => log.info("unknown message stereotype {}")
    }
    case _ => log.info("unknown message type {}")
  }
}

object Session {
  def props(id: Long, connection: ActorRef, taskService:ActorRef) = Props(
    new Session(id, connection, taskService)
  )
}
