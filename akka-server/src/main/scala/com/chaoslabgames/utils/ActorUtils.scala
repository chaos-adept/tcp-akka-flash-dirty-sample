package com.chaoslabgames.utils

import java.util.Optional
import java.util.concurrent.TimeUnit

import akka.actor.{ActorSystem, ActorSelection, ActorRef, ActorNotFound}
import akka.util.Timeout
import org.omg.CORBA.TIMEOUT

import scala.concurrent.{Future, Await}

/**
 * Created by Julia on 06.09.2015.
 */
public object ActorUtils {


  def getSingleActorRefFromPath(system:ActorSystem, path:String):Optional[ActorRef] = {

    try {
      // create an ActorSelection based on the path
      val sel = system.actorSelection(path)
      // check if a single actor exists at the path
      val fut = sel.resolveOne(TIMEOUT)
      ActorRef ref = Await.result(fut, TIMEOUT.duration())
      return Optional.of(ref);
    } catch (ActorNotFound e) {
      return Optional.absent();
    }
  }
}
