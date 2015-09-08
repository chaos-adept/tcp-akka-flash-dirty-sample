package com.chaoslabgames

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import com.chaoslabgames.ActorTestUtils.fishForMsg
import com.chaoslabgames.RoomSpec.{AuthUser, RoomInfo, UserCred}
import com.chaoslabgames.auth.AuthService
import com.chaoslabgames.core._
import com.chaoslabgames.core.datavalue.DataValue.{CreateRoomData, RoomData}
import com.chaoslabgames.session.Session
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}

import scala.collection.mutable.ListBuffer


/**
 * @author <a href="mailto:denis.rykovanov@gmail.com">Denis Rykovanov</a>
 *         on 08.09.2015.
 */
class RoomSpec(_system: ActorSystem) extends TestKit(_system) with ImplicitSender with FlatSpecLike with Matchers with BeforeAndAfterAll {
  def this() = this(ActorSystem("akka-server"))

  val aOwner1 = new UserCred("owner test 1", "test")
  val aOwner2 = new UserCred("owner test 2", "test")
  val defUsersForRoom1 = Seq(aOwner1, new UserCred("test2", "test2"), new UserCred("test3", "test3"))
  val defUsersForRoom2 = Seq(aOwner2, new UserCred("test4", "test4"), new UserCred("test5", "test5"))

  val defRoomsInfo = Seq((defUsersForRoom1, aOwner1), (defUsersForRoom2, aOwner2))
  var roomsInfo:Seq[RoomInfo] = null
  var taskService:ActorRef = null


  override protected def beforeAll() = {
    taskService = system.actorOf(Props[TaskService], "task")
    val authService: ActorRef = system.actorOf(Props[AuthService], "auth")

    roomsInfo = defRoomsInfo.map {case (users, owner) =>
      var roomUsers = new ListBuffer[AuthUser]
      //register users
      val authUsers = users.map { user =>
        val connection: TestProbe = TestProbe()
        val session: ActorRef = system.actorOf(Session.props(1, connection.testActor, taskService))
        session ! RegisterCmd(AuthReqData(user.name, user.password))
        val authMsg = fishForMsg[AuthEvent](connection)

        AuthUser(authMsg.data.id, user, session, connection)
      }
      //create room
      val roomOwner = authUsers.find { _.userCred == owner }.get
      roomOwner.session ! CreateRoomCmd(CreateRoomData(s"room of ${roomOwner.userCred.name}"))
      val roomData = fishForMsg[RoomCreatedEvent](roomOwner.connection).data
      //join all to the room
      authUsers.foreach { authUser =>
        authUser.session ! JoinCmd(roomData.roomId)
        authUser.connection.expectMsg(JoinEvent(authUser.userId, roomData.roomId))
      }
      RoomInfo(roomData, authUsers.toSet)
    }
  }

  override def afterAll() = {
    TestKit.shutdownActorSystem(system)
  }

  it should " send message to all users in the room " in {
    val targetRoom = roomsInfo.head
    val author = targetRoom.users.head

    author.session ! ChatCmd("hi room", targetRoom.roomData.roomId)
    
    targetRoom.users.foreach { user =>
      user.connection.expectMsg(ChatEvent("hi room", targetRoom.roomData.roomId, author.userId))
    }
  }

}

object RoomSpec {
  case class RoomInfo(roomData:RoomData, users:Set[AuthUser])
  case class UserCred(name:String, password:String)
  case class AuthUser(userId:Long, userCred:UserCred, session: ActorRef, connection: TestProbe)

}
