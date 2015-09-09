
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
import com.chaoslabgames.packet.RegisterCmd;

public class NetService
{
    private var ags: AgsEngine;

    public var main: Starter;

    private var userCred:UserCred;

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
            trace("connection failed")
        }
    }

    private function onDataReceived( e: Packet ): void
    {
        trace("onDataReceived cmd: " + e.Cmd)
        if( e.Cmd == CmdType.Ping )
        {

        }

        // ----- MAIN -----
        if( e.Cmd == CmdType.EVENT_Auth )
        {
//            var lr: LoginResp = new LoginResp();
//            lr.mergeFrom( e.Data );
//
//            main.addPlayer(lr.id.toNumber());
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

    public function join(): void
    {
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
        var r:RegisterCmd = new RegisterCmd();
        r.name = userCred.name;
        r.pass = userCred.password;
        r.writeTo(packet.Data);
        ags.send(packet)
    }
}
}
