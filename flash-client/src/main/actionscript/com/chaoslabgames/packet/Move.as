package com.chaoslabgames.packet {
	import com.netease.protobuf.*;
	import com.netease.protobuf.fieldDescriptors.*;
	import flash.utils.Endian;
	import flash.utils.IDataInput;
	import flash.utils.IDataOutput;
	import flash.utils.IExternalizable;
	import flash.errors.IOError;
	import com.chaoslabgames.packet.Point;
	use namespace used_by_generated_code;
	// @@protoc_insertion_point(imports)

	// @@protoc_insertion_point(class_metadata)
	public final class Move extends com.netease.protobuf.Message {
		/**
		 *  @private
		 */
		public static const POINT:RepeatedFieldDescriptor_TYPE_MESSAGE = new RepeatedFieldDescriptor_TYPE_MESSAGE("com.chaoslabgames.packet.Move.point", "point", (1 << 3) | com.netease.protobuf.WireType.LENGTH_DELIMITED, function():Class { return com.chaoslabgames.packet.Point; });

		[ArrayElementType("com.chaoslabgames.packet.Point")]
		public var point:Array = [];

		/**
		 *  @private
		 */
		override used_by_generated_code final function writeToBuffer(output:com.netease.protobuf.WritingBuffer):void {
			for (var point$index:uint = 0; point$index < this.point.length; ++point$index) {
				com.netease.protobuf.WriteUtils.writeTag(output, com.netease.protobuf.WireType.LENGTH_DELIMITED, 1);
				com.netease.protobuf.WriteUtils.write_TYPE_MESSAGE(output, this.point[point$index]);
			}
			for (var fieldKey:* in this) {
				super.writeUnknown(output, fieldKey);
			}
		}

		/**
		 *  @private
		 */
		override used_by_generated_code final function readFromSlice(input:flash.utils.IDataInput, bytesAfterSlice:uint):void {
			while (input.bytesAvailable > bytesAfterSlice) {
				var tag:uint = com.netease.protobuf.ReadUtils.read_TYPE_UINT32(input);
				switch (tag >> 3) {
				case 1:
					this.point.push(com.netease.protobuf.ReadUtils.read_TYPE_MESSAGE(input, new com.chaoslabgames.packet.Point()));
					break;
				default:
					super.readUnknown(input, tag);
					break;
				}
			}
		}

	}
}
