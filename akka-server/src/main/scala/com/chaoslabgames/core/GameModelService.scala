package com.chaoslabgames.core

import akka.actor.{Actor, ActorLogging}
import com.chaoslabgames.core.GameModelService._
import com.chaoslabgames.core.datavalue.DataValue.{AuthSessionInfo, ChatMsgData, RoomData, RoomListData}

import scala.collection.mutable
import scala.collection.mutable.{HashMap, HashSet}

/**
 * @author <a href="mailto:denis.rykovanov@gmail.com">Denis Rykovanov</a>
 *         on 08.09.2015.
 */
class GameModelService extends Actor with ActorLogging {

  var idCounter: Long = 0l
  val rooms = new mutable.HashMap[Long, Room]

  override def receive = {
    case CreateRoom(session, roomName) =>
      idCounter += 1
      val roomData:RoomData = new RoomData(roomName, idCounter, session.userId)
      rooms.put(roomData.roomId, new Room(roomData, new mutable.HashSet[AuthSessionInfo]()))
      session ! RoomCreatedEvent(roomData)
    case ListRoom(session) =>
      session ! RoomListEvent(RoomListData(rooms.values.map(r => r.initData).toSet))
    case JoinRoom(session, roomId) =>
      val room = rooms.get(roomId)
      if (room.isEmpty) {
        //todo send error event to user
        log.error("try send chat message to not existed room {}", roomId)
      } else {
        room.get.users.add(session)
      }
      session ! JoinEvent(session.userId, roomId)
    case LeaveRoom(session, roomId) =>
      val room = rooms.get(roomId)
      if (room.isEmpty) {
        //todo send error event to user
        log.error("try send chat message to not existed room {}", roomId)
      } else {
        room.get.users.remove(session)
      }
      session ! LeaveEvent(session.userId, roomId)
    case Chat(data) =>
      val room = rooms.get(data.roomId)
      if (room.isEmpty) {
        //todo send error event to user
        log.error("try send chat message to not existed room {}", data.roomId)
      } else {
        room.get.users.foreach( user =>
          user ! ChatEvent(data.text, data.author.userId, data.roomId)
        )
      }
  }
}

object GameModelService {
  class Room(val initData:RoomData, val users:mutable.Set[AuthSessionInfo])
  case class CreateRoom(session:AuthSessionInfo, roomName:String)
  case class JoinRoom(session:AuthSessionInfo, roomId:Long)
  case class LeaveRoom(session:AuthSessionInfo, roomId:Long)
  case class ListRoom(session:AuthSessionInfo)
  case class Chat(data:ChatMsgData)
}