package com.chaoslabgames.tcpfront

import java.nio.ByteBuffer

import akka.actor._
import akka.io.Tcp
import akka.io.Tcp.Write
import akka.io.TcpPipelineHandler.{Init, WithinActorContext}
import akka.util.ByteString
import com.chaoslabgames.core.{ActorSelectors, Cmd, Msg, TaskService}
import com.chaoslabgames.packet.PacketMSG
import com.chaoslabgames.session.Session
import com.chaoslabgames.utils.ActorUtils

import scala.concurrent.duration._

/**
 * @author <a href="mailto:denis.rykovanov@gmail.com">Denis Rykovanov</a>
 *         on 05.09.2015.
 */
class TcpConnection(
               val id: Long,
               tcpAdapter: ActorRef,
               init: Init[WithinActorContext, ByteString, ByteString]
               ) extends Actor with ActorLogging {

  import TcpConnection._
  import context._

  val taskService = ActorUtils.getSingleActorRefFromPath(context, ActorSelectors.task) //fixme move to factory actor

  // -----
  val session = context.actorOf(Session.props(id, self, taskService.get())) //fixme move to factory actor

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

    //fixme parse bytearray to an object for removing protobuf dependency
    session ! comm
    log.debug("receive data")
  }

  def sendData(cmd: Msg, data: Array[Byte]) {
    val trp: PacketMSG.Builder = PacketMSG.newBuilder()
    trp.setCmd(cmd.code)
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
  def props(id: Long, connect: ActorRef, init: Init[WithinActorContext, ByteString, ByteString]) = Props(
    new TcpConnection(id, connect, init)
  )

  // ----- API -----
  // Sending message to client
  case class Send(cmd: Msg, data: Array[Byte])
  // Checking client connection for life
  case object Heartbeat
}