package com.chaoslabgames.core.datavalue

import akka.actor.ActorRef

/**
 * @author <a href="mailto:denis.rykovanov@gmail.com">Denis Rykovanov</a>
 *         on 08.09.2015.
 */
object DataValue {
  case class CreateRoomData(name:String)
  case class RoomData(name:String, roomId:Long, ownerId:Long)
  case class RoomListData(rooms:Set[RoomData])
  case class AuthSessionInfo(userId:Long, out:ActorRef) { def !(msg:Any) = { out ! msg } }
  case class RoomSessionInfo(roomId:Long, session: AuthSessionInfo)
}
