package com.chaoslabgames

import akka.testkit.TestProbe

/**
 * @author <a href="mailto:denis.rykovanov@gmail.com">Denis Rykovanov</a>
 *         on 08.09.2015.
 */
object ActorTestUtils {
  def fishForMsg[T](probe:TestProbe):T = {
    probe.fishForMessage(){ case _:T => true }.asInstanceOf[T]
  }
}
