package com.chaoslabgames

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import com.chaoslabgames.auth.AuthService
import com.chaoslabgames.core._
import com.chaoslabgames.core.datavalue.DataValue.{RoomData, CreateRoomData}
import com.chaoslabgames.core.user.User
import com.chaoslabgames.session.Session
import org.scalatest._

/**
 * @author <a href="mailto:denis.rykovanov@gmail.com">Denis Rykovanov</a>
 *         on 06.09.2015.
 */
class UserIntegrationSpec(_system: ActorSystem) extends TestKit(_system) with ImplicitSender with FlatSpecLike with Matchers with BeforeAndAfterAll {
  def this() = this(ActorSystem("akka-server"))

  val aUser = new User(1, "test", "test")
  val aRoom = RoomData("room 1", 1, aUser.id)

  case class TestActorsRefs(conn: TestProbe, taskService: ActorRef, session: ActorRef, authService: ActorRef)

  def withEmptyActors(testCode: TestActorsRefs => Any) {
    val connectionProbe: TestProbe = TestProbe()
    val taskService: ActorRef = system.actorOf(Props[TaskService], "task")
    val authService: ActorRef = system.actorOf(Props[AuthService], "auth")
    val gameModelService: ActorRef = system.actorOf(Props[GameModelService], "gameModel")
    val session: ActorRef = system.actorOf(Session.props(1, connectionProbe.testActor, taskService))

    try {
      testCode(TestActorsRefs(connectionProbe, taskService, session, authService)) // "loan" the fixture to the test
    }
    finally {
      system.stop(session)
      system.stop(authService)
      system.stop(taskService)
      system.stop(gameModelService)
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

  def withRegisteredUser(user:User, testCode:(TestActorsRefs, User) => Any) = {
    withEmptyActors { actors =>
      actors.session ! RegisterCmd(AuthReqData(user.name, user.password))
      actors.conn.expectMsgClass(classOf[AuthEvent])
      testCode(actors, user)
    }
  }

  def withRoom(owner:User, room:RoomData, testCode:(TestActorsRefs, User, RoomData) => Any) ={
    withRegisteredUser(owner, { (actors, user) =>
      actors.session ! CreateRoomCmd(CreateRoomData("test room"))
      actors.conn.expectMsgClass(classOf[RoomCreatedEvent])
      testCode(actors, user, room)
    })
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

  it should "auth registered" in withRegisteredUser(aUser, {
    (actors: TestActorsRefs, user: User) =>
    actors.session ! AuthCmd(AuthReqData(user.name, user.password))
    actors.conn.expectMsgClass(classOf[AuthEvent])
  })

  it should "unauth is not able to create room" in withEmptyActors({ (actors) =>
    actors.session ! CreateRoomCmd(CreateRoomData("test room"))
    actors.conn.expectMsg(AuthRequiredEvent(AuthFailedData(2)))
  })

  it should "auth user is able to create room" in withRegisteredUser(aUser, { (actors, user) =>
    actors.session ! CreateRoomCmd(CreateRoomData("test room"))
    actors.conn.expectMsg(RoomCreatedEvent(RoomData("test room", 1, 1)))
  })

  it should "allow user to join to the room" in withRoom(aUser, aRoom, { (actors, user, room) =>
    actors.session ! JoinCmd(room.roomId)
    actors.conn.expectMsg(JoinEvent(aUser.id, room.roomId))
  })

  it should "allow user to leave from the room" in withRoom(aUser, aRoom, { (actors, user, room) =>
    actors.session ! LeaveCmd(room.roomId)
    actors.conn.expectMsg(LeaveEvent(aUser.id, room.roomId))
  })

//  it should "auth user is able to get list of rooms" in withRegisteredUser(aUser, { (actors, user) =>
//    actors.session ! CreateRoomCmd(CreateRoomData("room 1"))
//    actors.session ! CreateRoomCmd(CreateRoomData("room 2"))
//    actors.conn.expectMsgClass(classOf[RoomCreatedEvent])
//    actors.conn.expectMsgClass(classOf[RoomCreatedEvent])
//    actors.session ! GetRoomListCmd
//    actors.conn.expectMsg(RoomListEvent(RoomListData(Set(RoomData("room 1", 1,  aUser.id), RoomData("room 2", 2, aUser.id)))))
//  })

}