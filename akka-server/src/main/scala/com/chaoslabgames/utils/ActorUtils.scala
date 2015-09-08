package com.chaoslabgames.utils

import akka.actor._

import scala.concurrent.Await
import scala.concurrent.duration._

/**
 * Created by Julia on 06.09.2015.
 */
object ActorUtils {
  def getSingleActorRefFromPath(system:ActorContext, path:String):Option[ActorRef] = {
    try {
      // create an ActorSelection based on the path
      val sel = system.actorSelection(path)
      // check if a single actor exists at the path
      val fut = sel.resolveOne(100.microsecond)
      val ref:ActorRef = Await.result(fut, 100.microsecond)
      Option(ref)
    } catch {
      case e:ActorNotFound => Option.empty[ActorRef]
    }
  }
}
