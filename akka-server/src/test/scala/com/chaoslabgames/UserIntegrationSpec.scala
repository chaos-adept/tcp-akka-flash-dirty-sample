package com.chaoslabgames

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import com.chaoslabgames.auth.AuthService
import com.chaoslabgames.core._
import com.chaoslabgames.core.user.User
import com.chaoslabgames.session.Session
import org.scalatest._

/**
 * @author <a href="mailto:denis.rykovanov@gmail.com">Denis Rykovanov</a>
 *         on 06.09.2015.
 */
class UserIntegrationSpec(_system: ActorSystem) extends TestKit(_system) with ImplicitSender with FlatSpecLike with Matchers with BeforeAndAfterAll {
  def this() = this(ActorSystem("akka-server"))

  case class TestActorsRefs(conn: TestProbe, taskService: ActorRef, session: ActorRef, authService: ActorRef)

  def withEmptyActors(testCode: TestActorsRefs => Any) {
    val connectionProbe: TestProbe = TestProbe()
    val taskService: ActorRef = system.actorOf(Props[TaskService], "task")
    val authService: ActorRef = system.actorOf(Props[AuthService], "auth")
    val session: ActorRef = system.actorOf(Session.props(1, connectionProbe.testActor, taskService))

    try {
      testCode(TestActorsRefs(connectionProbe, taskService, session, authService)) // "loan" the fixture to the test
    }
    finally {
      system.stop(session)
      system.stop(authService)
      system.stop(taskService)
    }
  }


  def withRegisteredUsers(users: Set[User], testCode: (TestActorsRefs, Set[User]) => Any) = {
    withEmptyActors { actors =>
      users.foreach { user =>
        actors.session ! RegisterCmd(AuthReqData(user.name, user.password))
        actors.conn.expectMsgClass(classOf[AuthEvent])
      }
      testCode(actors, users)
    }
  }

  override def afterAll() = {
    TestKit.shutdownActorSystem(system)
  }

  it should "rejected if unknown" in withEmptyActors { actors =>
    actors.session ! AuthCmd(AuthReqData("test", "test"))
    actors.conn.expectMsg(AuthErrEvent(AuthFailedData(1)))
  }

  it should "register" in withEmptyActors { actors =>
    actors.session ! RegisterCmd(AuthReqData("test", "test"))
    actors.conn.expectMsg(AuthEvent(AuthRespData(1)))
  }

  it should "auth all registered" in withRegisteredUsers(Set(new User(1, "test", "test"), new User(2, "test2", "test2")), {
    (actors: TestActorsRefs, users: Set[User]) =>
      users.foreach { user =>
        actors.session ! AuthCmd(AuthReqData(user.name, user.password))
        actors.conn.expectMsgClass(classOf[AuthEvent])
      }
  })

}