package {

import com.chaoslabgames.NetService;
import com.chaoslabgames.datavalue.ServerConfig;
import com.chaoslabgames.datavalue.UserCred;
import com.chaoslabgames.packet.GetRoomListEventPkg;
import com.chaoslabgames.packet.JoinEventPkg;
import com.chaoslabgames.packet.RoomCreatedEventPkg;

import flash.display.Sprite;
import flash.events.Event;

[SWF(backgroundColor="#000000", frameRate="30", width="1280", height="800")]
public class Starter extends Sprite {

    public var net:NetService = new NetService();

    public function Starter() {
        net.main = this;
        this.addEventListener(Event.ADDED_TO_STAGE, function (e:Event):void {
            var serverConfig:ServerConfig = new ServerConfig();
            serverConfig.host = "127.0.0.1";
            serverConfig.port = 8899;
            net.connect(serverConfig);
        });
    }

    public function onAuth(userId:Number):void {
        trace("user was auth id: " + userId);
        net.createRoom("test room")
    }

    public function onConnected():void {
        var userCred:UserCred = new UserCred()
        userCred.name = "test1";
        userCred.password = "test1";
        net.register(userCred);
    }

    public function roomList(roomList:GetRoomListEventPkg):void {
        trace("room list: " + roomList.room);
    }

    public function roomCreated(event:RoomCreatedEventPkg):void {
        trace("room created " + event);
        net.join(event.roomId)
    }

    public function onJoinEvent(join:JoinEventPkg):void {
        trace("join event " + join);
    }
}
}
