package com.chaoslabgames

import akka.actor.{Props, ActorRef, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import com.chaoslabgames.core._
import com.chaoslabgames.session.Session
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

/**
 * @author <a href="mailto:denis.rykovanov@gmail.com">Denis Rykovanov</a>
 *         on 06.09.2015.
 */
class SessionSpec(_system: ActorSystem) extends TestKit(_system) with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll {
  def this() = this(ActorSystem("akka-server"))

  def withConnAndTask(testCode: (TestProbe, TestProbe, ActorRef) => Any) {
    try {
      val connectionProbe: TestProbe = TestProbe()
      val taskServiceProbe: TestProbe = TestProbe()

      val session: ActorRef = system.actorOf(Session.props(1, connectionProbe.testActor, taskServiceProbe.testActor))

      testCode(connectionProbe, taskServiceProbe, session) // "loan" the fixture to the test
    }
    finally {

    }
  }

  override def afterAll() = {
    TestKit.shutdownActorSystem(system)
  }

  "An Session" must {
    "send request area-classes to task actor" in withConnAndTask{ {
      (connection, taskService, session) =>
        session ! AuthCmd(AuthReqData("test", "test"))
        taskService.reply(AuthEvent(AuthRespData(123)))
        taskService.expectMsg(AuthCmd(AuthReqData("test", "test")))
        connection.expectMsg(AuthEvent(AuthRespData(123)))
    }}
  }
}