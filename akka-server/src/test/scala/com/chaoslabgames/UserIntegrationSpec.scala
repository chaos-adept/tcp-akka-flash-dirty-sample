package com.chaoslabgames

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import com.chaoslabgames.auth.AuthService
import com.chaoslabgames.core._
import com.chaoslabgames.session.Session
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

/**
 * @author <a href="mailto:denis.rykovanov@gmail.com">Denis Rykovanov</a>
 *         on 06.09.2015.
 */
class UserIntegrationSpec(_system: ActorSystem) extends TestKit(_system) with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll {
  def this() = this(ActorSystem("akka-server"))

  case class SessionTestArgs(conn: TestProbe, taskService: ActorRef, session: ActorRef)

  def withConnAndTask(testCode: SessionTestArgs => Any) {
    val connectionProbe: TestProbe = TestProbe()
    val taskService: ActorRef = system.actorOf(Props[TaskService], "task")
    val authService: ActorRef = system.actorOf(Props[AuthService], "auth")
    val session: ActorRef = system.actorOf(Session.props(1, connectionProbe.testActor, taskService))

    try {
      testCode(SessionTestArgs(connectionProbe, taskService, session)) // "loan" the fixture to the test
    }
    finally {
      system.stop(session)
      system.stop(authService)
      system.stop(taskService)
    }
  }

  override def afterAll() = {
    TestKit.shutdownActorSystem(system)
  }

  "User" must {
    "rejected if unknown" in {
      withConnAndTask { actors => {
        actors.session ! AuthCmd(AuthReqData("test", "test"))
        actors.conn.expectMsg(AuthErrEvent(AuthFailedData(1)))
      }
      }
    }
    "register" in {
      withConnAndTask { actors => {
        actors.session ! RegisterCmd(AuthReqData("test", "test"))
        actors.conn.expectMsg(AuthEvent(AuthRespData(1)))
      }
      }
    }
    "auth for registered" in {
      withConnAndTask { actors => {
        actors.session ! RegisterCmd(AuthReqData("test", "test"))
        actors.session ! AuthCmd(AuthReqData("test", "test"))
        actors.conn.expectMsg(AuthEvent(AuthRespData(1)))
      }
      }
    }

  }
}