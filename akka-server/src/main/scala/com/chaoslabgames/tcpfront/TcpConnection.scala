package com.chaoslabgames.tcpfront

import java.nio.ByteBuffer

import akka.actor._
import akka.io.Tcp
import akka.io.Tcp.Write
import akka.io.TcpPipelineHandler.{Init, WithinActorContext}
import akka.util.ByteString
import com.chaoslabgames.core.datavalue.DataValue.CreateRoomData
import com.chaoslabgames.core.{RegisterCmd => InternalRegCmd, _}
import com.chaoslabgames.packet._
import com.chaoslabgames.session.Session

import scala.collection.mutable.ListBuffer
import scala.concurrent.duration._

/**
 * @author <a href="mailto:denis.rykovanov@gmail.com">Denis Rykovanov</a>
 *         on 05.09.2015.
 */
class TcpConnection(
               val id: Long,
               tcpAdapter: ActorRef, taskService:ActorRef,
               init: Init[WithinActorContext, ByteString, ByteString]
               ) extends Actor with ActorLogging {

  import TcpConnection._
  import context._


  // -----
  lazy val session = context.actorOf(Session.props(id, self, taskService)) //fixme move to factory actor

  // ----- heartbeat -----
  private var scheduler: Cancellable = _

  // ----- actor -----
  override def preStart() {
    // initialization code
    scheduler = context.system.scheduler.schedule(period, period, self, Heartbeat)

    //fixme move to a builder / registrator
    log.info("Session start: {}", toString)
  }

  override def receive = {
    case init.Event(data) => receiveData(data)

    case Send(cmd, data) => sendData(cmd, data)

    case Heartbeat => sendHeartbeat()

    case _: Tcp.ConnectionClosed => Closed()

    case ae:AuthEvent =>
      val pkg = AuthEventPkg
        .newBuilder()
        .setId(ae.data.id).build()
      sendData(ae.msg, pkg.toByteArray)
    case rl:RoomListEvent =>
      var rooms = new ListBuffer[RoomMsgPkg]()
      val roomsPkgs = rl.data.rooms.map { roomData =>
        RoomMsgPkg.newBuilder()
          .setRoomId(roomData.roomId)
          .setName(roomData.name).build()
      }
      val listPkg = GetRoomListEventPkg.newBuilder()
      roomsPkgs.foreach(listPkg.addRoom)
      sendData(rl.msg, listPkg.build().toByteArray)
    case rce:RoomCreatedEvent =>
      sendData(rce.msg, RoomCreatedEventPkg.newBuilder()
        .setName(rce.data.name)
        .setRoomId(rce.data.roomId)
        .setUserId(rce.data.ownerId)
        .build().toByteArray
      )
    case e:JoinEvent =>
      sendData(e.msg, JoinEventPkg.newBuilder()
        .setRoomId(e.roomId)
        .setUserId(e.userId)
        .build().toByteArray
      );

    case _ => log.info("unknown message")
  }

  override def postStop() {
    // clean up resources
    scheduler.cancel()

    log.info("Session stop: {}", toString)
  }

  // ----- actions -----
  def receiveData(data: ByteString) {
    val comm: PacketMSG = PacketMSG.parseFrom( data.toArray )

    comm.getType match {
      case Cmd.Auth.code =>
        val loginPkg:LoginCmdPkg = LoginCmdPkg.parseFrom(comm.getData)
        session ! AuthCmd(AuthReqData(loginPkg.getName, loginPkg.getPass))
      case Cmd.Register.code =>
        val pkg:RegisterCmdPkg = RegisterCmdPkg.parseFrom(comm.getData)
        session ! InternalRegCmd(AuthReqData(pkg.getName, pkg.getPass))
      case Cmd.GetRoomList.code =>
        session ! GetRoomListCmd
      case Cmd.CreateRoom.code =>
        val pkg = CreateRoomCmdPkg.parseFrom(comm.getData)
        session ! CreateRoomCmd(CreateRoomData(pkg.getName))
      case Cmd.Join.code =>
        val pkg = JoinCmdPkg.parseFrom(comm.getData)
        session ! JoinCmd(pkg.getRoomId)
    }

    session ! comm
    log.debug("receive data")
  }

  def sendData(cmd: Msg, data: Array[Byte]) {
    val trp: PacketMSG.Builder = PacketMSG.newBuilder()
    trp.setType(cmd.code)
    trp.setData(com.google.protobuf.ByteString.copyFrom(data))

    val packet = trp.build().toByteArray
    val bb: ByteBuffer = ByteBuffer.allocate(4 + packet.length)
    bb.putInt(packet.length)
    bb.put(packet)

    val msg: ByteString = ByteString(bb.array())

    tcpAdapter ! Write(msg)

    log.info("Cmd send: {}", cmd)
  }

  def sendHeartbeat(): Unit = {
    sendData(Cmd.Ping, Array[Byte]())
  }

  def Closed() {
    context stop self
  }

  // ----- override -----
  override def toString = "{ Id: %d }".format(id)
}

object TcpConnection {

  // ----- heartbeat -----
  val period = 10.seconds

  // safe constructor
  def props(id: Long, connect: ActorRef, taskService:ActorRef, init: Init[WithinActorContext, ByteString, ByteString]) = Props(
    new TcpConnection(id, connect, taskService, init)
  )

  // ----- API -----
  // Sending message to client
  case class Send(cmd: Msg, data: Array[Byte])
  // Checking client connection for life
  case object Heartbeat
}