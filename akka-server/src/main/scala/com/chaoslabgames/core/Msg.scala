package com.chaoslabgames.core

import com.chaoslabgames.core.datavalue.DataValue.{CreateRoomData, RoomData, RoomListData}

/**
 * @author <a href="mailto:denis.rykovanov@gmail.com">Denis Rykovanov</a>
 *         on 05.09.2015.
 */
sealed trait Msg { def code: Int; def stereotype: Int; def authRequired:Boolean = false }

case object Cmd {
  val TYPE_CMD = 1
  val TYPE_EVENT = 2
  case object Ping     extends Msg { val code = 1; val stereotype = TYPE_CMD }
  case object Auth     extends Msg { val code = 5; val stereotype = TYPE_CMD }
  case object AuthResp extends Msg { val code = 6; val stereotype = TYPE_EVENT }
  case object AuthErr  extends Msg { val code = 7; val stereotype = TYPE_EVENT }
  case object Join     extends Msg { val code = 8; val stereotype = TYPE_EVENT; override val authRequired = true }
  case object Move     extends Msg { val code = 9; val stereotype = TYPE_CMD }

  case object CreateRoom      extends Msg { val code = 11; val stereotype = TYPE_CMD; override val authRequired = true }
  case object CreatedRoom      extends Msg { val code = 12; val stereotype = TYPE_EVENT; override val authRequired = true }
  case object Register        extends Msg { val code = 13; val stereotype = TYPE_CMD }
  case object GetRoomList        extends Msg { val code = 14; val stereotype = TYPE_CMD; override val authRequired = true }
  case object RetRoomList        extends Msg { val code = 15; val stereotype = TYPE_EVENT }
  case object Joinned        extends Msg { val code = 16; val stereotype = TYPE_EVENT }
  case object Leave        extends Msg { val code = 17; val stereotype = TYPE_CMD; override val authRequired = true}
  case object Leaved        extends Msg { val code = 18; val stereotype = TYPE_EVENT }
  case object ChatCmd        extends Msg { val code = 19; val stereotype = TYPE_CMD; override val authRequired = true }
  case object ChatEvent        extends Msg { val code = 20; val stereotype = TYPE_EVENT }
}

class Cmd(val msg:Msg, val data:Any)

case class AuthReqData(name:String, password:String)
case class AuthRespData(id:Long)
case class AuthFailedData(reason:Int)


case class AuthCmd(override val data:AuthReqData) extends Cmd(Cmd.Auth, data)
case class RegisterCmd(override val data:AuthReqData) extends Cmd(Cmd.Register, data)
case class AuthEvent(override val data:AuthRespData) extends Cmd(Cmd.AuthResp, data)
case class AuthRequiredEvent(override val data:AuthFailedData) extends Cmd(Cmd.AuthResp, data)
case class AuthErrEvent(override val data:AuthFailedData) extends Cmd(Cmd.AuthErr, data)
case class CreateRoomCmd(override val data:CreateRoomData) extends Cmd(Cmd.CreateRoom, data)
case class RoomCreatedEvent(override val data:RoomData) extends Cmd(Cmd.CreatedRoom, data)
case object GetRoomListCmd extends Cmd(Cmd.GetRoomList, null)
case class RoomListEvent(override val data:RoomListData) extends Cmd(Cmd.RetRoomList, data)
case class JoinEvent(userId:Long, roomId:Long) extends Cmd(Cmd.Joinned, null)

case class JoinCmd(roomId:Long) extends Cmd(Cmd.Join, null)
case class LeaveCmd(roomId:Long) extends Cmd(Cmd.Leave, null)
case class LeaveEvent(userId:Long, roomId:Long) extends Cmd(Cmd.Leaved, null)

case class ChatCmd(text:String, roomId:Long) extends Cmd(Cmd.ChatCmd, null)
case class ChatEvent(text:String, userId:Long, roomId:Long) extends Cmd(Cmd.ChatEvent, null)


