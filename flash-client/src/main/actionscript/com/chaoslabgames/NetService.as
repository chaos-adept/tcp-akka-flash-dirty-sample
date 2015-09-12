
package com.chaoslabgames
{
import com.awar.ags.api.ConnectionResponse;
import com.awar.ags.api.MessageType;
import com.awar.ags.api.Packet;
import com.awar.ags.connection.AvailableConnection;
import com.awar.ags.connection.TransportType;
import com.awar.ags.engine.AgsEngine;
import com.awar.ags.engine.Server;
import com.chaoslabgames.datavalue.ServerConfig;
import com.chaoslabgames.datavalue.UserCred;
import com.chaoslabgames.packet.AuthEventPkg;
import com.chaoslabgames.packet.ChatCmdPkg;
import com.chaoslabgames.packet.ChatEventPkg;
import com.chaoslabgames.packet.CreateRoomCmdPkg;
import com.chaoslabgames.packet.GetRoomListEventPkg;
import com.chaoslabgames.packet.JoinCmdPkg;
import com.chaoslabgames.packet.JoinEventPkg;
import com.chaoslabgames.packet.RegisterCmdPkg;
import com.chaoslabgames.packet.RoomCreatedEventPkg;
import com.furusystems.logging.slf4as.ILogger;
import com.furusystems.logging.slf4as.Logging;
import com.netease.protobuf.Int64;

public class NetService
{
    private var ags: AgsEngine;

    public var main: Starter;

    private var userCred:UserCred;

    public static const log:ILogger = Logging.getLogger(NetService);

    public function NetService()
    {
        ags = new AgsEngine();
        ags.addEventListener( MessageType.ConnectionAttemptResponse.name, onConnectionResponse );
        ags.addEventListener( MessageType.ConnectionResponse.name, onConnectionResponse );
        ags.addEventListener( MessageType.Packet.name, onDataReceived );
    }

    public function connect(serverConf:ServerConfig): void
    {
        this.userCred = userCred;

        var server: Server = new Server( "server1" );
        var availConn: AvailableConnection = new AvailableConnection( serverConf.host, serverConf.port, TransportType.TCP );
        server.addAvailableConnection( availConn );
        ags.addServer( server );

        ags.connect();
    }

    private function onConnectionResponse( e: ConnectionResponse ): void
    {
        if( e.successful )
        {
            main.onConnected()
        } else {
            log.error("connection failed")
        }
    }

    private function onDataReceived( e: Packet ): void
    {

        if( e.Cmd == CmdType.Ping )
        {
            return
        }
        log.info("onDataReceived cmd: " + e.Cmd)
        // ----- MAIN -----
        if( e.Cmd == CmdType.EVENT_Auth )
        {
            var ae: AuthEventPkg = new AuthEventPkg();
            ae.mergeFrom(e.Data);
            main.onAuth(ae.id.toNumber());
        }

        if (e.Cmd == CmdType.EVENT_RoomList) {
            var roomList:GetRoomListEventPkg = new GetRoomListEventPkg();
            roomList.mergeFrom(e.Data);
            main.onRoomList(roomList)
        }

        if (e.Cmd == CmdType.EVENT_CreateRoomEvent) {
            var createRoomEvent:RoomCreatedEventPkg = new RoomCreatedEventPkg();
            createRoomEvent.mergeFrom(e.Data);
            main.onRoomCreated(createRoomEvent)
        }

        if (e.Cmd == CmdType.EVENT_Join) {
            var joinEvent:JoinEventPkg = new JoinEventPkg();
            joinEvent.mergeFrom(e.Data);
            main.onJoinEvent(joinEvent)
        }

        if (e.Cmd == CmdType.EVENT_Chat) {
            var chatEvent:ChatEventPkg = new ChatEventPkg();
            chatEvent.mergeFrom(e.Data);
            main.onChatEvent(chatEvent)
        }

    }

    public function login( login: String, pass: String ): void
    {
//        var packet: Packet = new Packet();
//        packet.Cmd = CmdType.Auth;
//
//        var lr: Login = new Login();
//        lr.name = login;
//        lr.pass = pass;
//
//        lr.writeTo( packet.Data );
//
//        ags.send( packet );
    }

    public function join(roomId:Int64):void
    {
        var packet:Packet = new Packet();
        packet.Cmd = CmdType.CMD_Join;
        var joinPkg:JoinCmdPkg = new JoinCmdPkg();
        joinPkg.roomId = roomId;
        joinPkg.writeTo(packet.Data);
        ags.send( packet );
//        var packet: Packet = new Packet();
//        packet.Cmd = CmdType.Join;
//
//        var lr: Join = new Join();
//
//        lr.writeTo( packet.Data );
//
//        ags.send( packet );
    }


    public function register(userCred:UserCred):void {
        var packet: Packet = new Packet();
        packet.Cmd = CmdType.CMD_Register;
        var r:RegisterCmdPkg = new RegisterCmdPkg();
        r.name = userCred.name;
        r.pass = userCred.password;
        r.writeTo(packet.Data);
        ags.send(packet)
    }

    public function createRoom(name:String):void {
        var packet: Packet = new Packet();
        packet.Cmd = CmdType.CMD_CreateRoom;
        var r:CreateRoomCmdPkg = new CreateRoomCmdPkg();
        r.name = name;
        r.writeTo(packet.Data);
        ags.send(packet)
    }

    public function listRooms():void {
        log.info("mock room list")
    }

    public function sendChatMessage(roomId:Int64, text:String):void {
        var packet:ChatCmdPkg = new ChatCmdPkg();
        packet.roomId = roomId;
        packet.text = text;
        sendPacket(CmdType.CMD_Chat, packet)
    }

    private function sendPacket(cmd:int, args:com.netease.protobuf.Message):void {
        var packet: Packet = new Packet();
        packet.Cmd = cmd;
        args.writeTo(packet.Data);
        ags.send(packet)
    }
}
}
