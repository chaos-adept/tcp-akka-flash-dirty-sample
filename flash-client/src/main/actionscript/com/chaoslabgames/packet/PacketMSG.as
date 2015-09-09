package com.chaoslabgames.packet {
	import com.netease.protobuf.*;
	import com.netease.protobuf.fieldDescriptors.*;
	import flash.utils.Endian;
	import flash.utils.IDataInput;
	import flash.utils.IDataOutput;
	import flash.utils.IExternalizable;
	import flash.errors.IOError;
	import flash.utils.ByteArray;
	use namespace used_by_generated_code;
	// @@protoc_insertion_point(imports)

	// @@protoc_insertion_point(class_metadata)
	public final class PacketMSG extends com.netease.protobuf.Message {
		/**
		 *  @private
		 */
		public static const PING:FieldDescriptor_TYPE_BOOL = new FieldDescriptor_TYPE_BOOL("com.chaoslabgames.packet.PacketMSG.ping", "ping", (1 << 3) | com.netease.protobuf.WireType.VARINT);

		private var ping$field:Boolean;

		private var hasField$0:uint = 0;

		public function clearPing():void {
			hasField$0 &= 0xfffffffe;
			ping$field = new Boolean();
		}

		public function get hasPing():Boolean {
			return (hasField$0 & 0x1) != 0;
		}

		public function set ping(value:Boolean):void {
			hasField$0 |= 0x1;
			ping$field = value;
		}

		public function get ping():Boolean {
			return ping$field;
		}

		/**
		 *  @private
		 */
		public static const TYPE:FieldDescriptor_TYPE_INT32 = new FieldDescriptor_TYPE_INT32("com.chaoslabgames.packet.PacketMSG.type", "type", (2 << 3) | com.netease.protobuf.WireType.VARINT);

		private var type$field:int;

		public function clearType():void {
			hasField$0 &= 0xfffffffd;
			type$field = new int();
		}

		public function get hasType():Boolean {
			return (hasField$0 & 0x2) != 0;
		}

		public function set type(value:int):void {
			hasField$0 |= 0x2;
			type$field = value;
		}

		public function get type():int {
			return type$field;
		}

		/**
		 *  @private
		 */
		public static const DATA:FieldDescriptor_TYPE_BYTES = new FieldDescriptor_TYPE_BYTES("com.chaoslabgames.packet.PacketMSG.data", "data", (3 << 3) | com.netease.protobuf.WireType.LENGTH_DELIMITED);

		private var data$field:flash.utils.ByteArray;

		public function clearData():void {
			data$field = null;
		}

		public function get hasData():Boolean {
			return data$field != null;
		}

		public function set data(value:flash.utils.ByteArray):void {
			data$field = value;
		}

		public function get data():flash.utils.ByteArray {
			return data$field;
		}

		/**
		 *  @private
		 */
		override used_by_generated_code final function writeToBuffer(output:com.netease.protobuf.WritingBuffer):void {
			if (hasPing) {
				com.netease.protobuf.WriteUtils.writeTag(output, com.netease.protobuf.WireType.VARINT, 1);
				com.netease.protobuf.WriteUtils.write_TYPE_BOOL(output, ping$field);
			}
			if (hasType) {
				com.netease.protobuf.WriteUtils.writeTag(output, com.netease.protobuf.WireType.VARINT, 2);
				com.netease.protobuf.WriteUtils.write_TYPE_INT32(output, type$field);
			}
			if (hasData) {
				com.netease.protobuf.WriteUtils.writeTag(output, com.netease.protobuf.WireType.LENGTH_DELIMITED, 3);
				com.netease.protobuf.WriteUtils.write_TYPE_BYTES(output, data$field);
			}
			for (var fieldKey:* in this) {
				super.writeUnknown(output, fieldKey);
			}
		}

		/**
		 *  @private
		 */
		override used_by_generated_code final function readFromSlice(input:flash.utils.IDataInput, bytesAfterSlice:uint):void {
			var ping$count:uint = 0;
			var type$count:uint = 0;
			var data$count:uint = 0;
			while (input.bytesAvailable > bytesAfterSlice) {
				var tag:uint = com.netease.protobuf.ReadUtils.read_TYPE_UINT32(input);
				switch (tag >> 3) {
				case 1:
					if (ping$count != 0) {
						throw new flash.errors.IOError('Bad data format: PacketMSG.ping cannot be set twice.');
					}
					++ping$count;
					this.ping = com.netease.protobuf.ReadUtils.read_TYPE_BOOL(input);
					break;
				case 2:
					if (type$count != 0) {
						throw new flash.errors.IOError('Bad data format: PacketMSG.type cannot be set twice.');
					}
					++type$count;
					this.type = com.netease.protobuf.ReadUtils.read_TYPE_INT32(input);
					break;
				case 3:
					if (data$count != 0) {
						throw new flash.errors.IOError('Bad data format: PacketMSG.data cannot be set twice.');
					}
					++data$count;
					this.data = com.netease.protobuf.ReadUtils.read_TYPE_BYTES(input);
					break;
				default:
					super.readUnknown(input, tag);
					break;
				}
			}
		}

	}
}
