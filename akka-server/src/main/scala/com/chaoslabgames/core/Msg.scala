package com.chaoslabgames.core

import com.chaoslabgames.core.Cmd.Auth

/**
 * @author <a href="mailto:denis.rykovanov@gmail.com">Denis Rykovanov</a>
 *         on 05.09.2015.
 */
sealed trait Msg { def code: Int; def stereotype: Int }

case object Cmd {
  val TYPE_CMD = 1
  val TYPE_EVENT = 2
  case object Ping     extends Msg { val code = 1; val stereotype = TYPE_CMD }
  case object Auth     extends Msg { val code = 5; val stereotype = TYPE_CMD }
  case object AuthResp extends Msg { val code = 6; val stereotype = TYPE_EVENT }
  case object AuthErr  extends Msg { val code = 7; val stereotype = TYPE_EVENT }
  case object Join     extends Msg { val code = 8; val stereotype = TYPE_EVENT }
  case object Move     extends Msg { val code = 9; val stereotype = TYPE_CMD }
}

class Cmd(val msg:Msg, val data:Any)
case class AuthReqData(name:String, password:String)
case class AuthRespData(id:Long)

case class AuthCmd(override val data:AuthReqData) extends Cmd(Cmd.Auth, data)
case class AuthEvent(override val data:AuthRespData) extends Cmd(Cmd.AuthResp, data)





