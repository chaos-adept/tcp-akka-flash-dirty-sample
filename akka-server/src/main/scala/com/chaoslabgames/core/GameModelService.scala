package com.chaoslabgames.core

import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorLogging}
import com.chaoslabgames.core.GameModelService.{LeaveRoom, JoinRoom, ListRoom, CreateRoom}
import com.chaoslabgames.core.datavalue.DataValue.{RoomListData, AuthSessionInfo, RoomData}

import scala.collection.mutable.ListBuffer

/**
 * @author <a href="mailto:denis.rykovanov@gmail.com">Denis Rykovanov</a>
 *         on 08.09.2015.
 */
class GameModelService extends Actor with ActorLogging {

  var idCounter: Long = 0l
  val rooms = new ListBuffer[RoomData]

  override def receive = {
    case CreateRoom(session, roomName) =>
      idCounter += 1
      val room = RoomData(roomName, idCounter, session.userId)
      rooms += room
      session ! RoomCreatedEvent(room)
    case ListRoom(session) =>
      session ! RoomListEvent(RoomListData(rooms.toSet))
    case JoinRoom(session, roomId) =>
      session ! JoinEvent(session.userId, roomId)
    case LeaveRoom(session, roomId) =>
      session ! LeaveEvent(session.userId, roomId)
  }

}

object GameModelService {
  case class CreateRoom(session:AuthSessionInfo, roomName:String)
  case class JoinRoom(session:AuthSessionInfo, roomId:Long)
  case class LeaveRoom(session:AuthSessionInfo, roomId:Long)
  case class ListRoom(session:AuthSessionInfo)
}