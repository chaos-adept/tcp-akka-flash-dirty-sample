// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: packet.proto

package com.chaoslabgames.packet;

public interface MoveOrBuilder extends
    // @@protoc_insertion_point(interface_extends:com.chaoslabgames.packet.Move)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>repeated .com.chaoslabgames.packet.Point point = 1;</code>
   */
  java.util.List<com.chaoslabgames.packet.Point> 
      getPointList();
  /**
   * <code>repeated .com.chaoslabgames.packet.Point point = 1;</code>
   */
  com.chaoslabgames.packet.Point getPoint(int index);
  /**
   * <code>repeated .com.chaoslabgames.packet.Point point = 1;</code>
   */
  int getPointCount();
  /**
   * <code>repeated .com.chaoslabgames.packet.Point point = 1;</code>
   */
  java.util.List<? extends com.chaoslabgames.packet.PointOrBuilder> 
      getPointOrBuilderList();
  /**
   * <code>repeated .com.chaoslabgames.packet.Point point = 1;</code>
   */
  com.chaoslabgames.packet.PointOrBuilder getPointOrBuilder(
      int index);
}