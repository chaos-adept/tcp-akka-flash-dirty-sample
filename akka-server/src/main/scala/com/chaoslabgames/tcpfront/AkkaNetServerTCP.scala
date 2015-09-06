package com.chaoslabgames.tcpfront

import java.net.InetSocketAddress
import java.nio.ByteOrder

import akka.actor.{Actor, ActorLogging, Props}
import akka.io.Tcp._
import akka.io.{LengthFieldFrame, TcpPipelineHandler, TcpReadWriteAdapter, _}


class AkkaNetServerTCP(address: String, port: Int) extends Actor with ActorLogging {
  var idCounter = 0L

  override def preStart() {
    log.info("Starting tcp net server")

    import context.system
    val opts = List(SO.KeepAlive(on = true), SO.TcpNoDelay(on = true))
    IO(Tcp) ! Bind(self, new InetSocketAddress(address, port), options = opts)
  }

  def receive = {
    case b@Bound(localAddress) =>
    // do some logging or setup ...
      log.info("server was started {}", localAddress)
    case CommandFailed(_: Bind) =>
      log.info("Command failed tcp server")
      context stop self

    case c@Connected(remote, local) =>
      log.info("New incoming tcp connection on server")
      createSession(remote, local)

  }

  // ----- handles -----
  def createSession(remote: InetSocketAddress, local: InetSocketAddress) = {
    val framer = new LengthFieldFrame(8192, ByteOrder.BIG_ENDIAN, 4, false)
    val init = TcpPipelineHandler.withLogger(log, framer >> new TcpReadWriteAdapter)

    idCounter += 1
    val tcpSessionProps = TcpConnection.props(idCounter, sender, init)
    val tcpSession = context.actorOf(tcpSessionProps)
    val pipeline = context.actorOf(TcpPipelineHandler.props(init, sender, tcpSession))

    sender ! Register(pipeline)

  }
}

object AkkaNetServerTCP {

  // safe constructor
  def props(address: String, port: Int) = Props(new AkkaNetServerTCP(address, port))
}
