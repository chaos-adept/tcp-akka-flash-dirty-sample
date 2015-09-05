package  {

import com.chaoslabgames.NetService;

import flash.display.Sprite;
import flash.events.Event;

[SWF(backgroundColor="#000000", frameRate="30", width="1280", height="800")]
	public class Starter extends Sprite {

		public var net: NetService = new NetService();

		public function Starter()
		{
			net.main = this;
			this.addEventListener(Event.ADDED_TO_STAGE, function (e:Event):void {
				net.connect("Tester1", "test");
			});
		}

	public function addPlayer(number:Number):void {
		trace("player was added " + number)
	}
}
}
