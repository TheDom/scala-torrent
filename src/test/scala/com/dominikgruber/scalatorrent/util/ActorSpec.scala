package com.dominikgruber.scalatorrent.util

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}


abstract class ActorSpec extends TestKit(ActorSystem())
  with ImplicitSender with WordSpecLike with Matchers
  with BeforeAndAfterAll with MockitoSugar {


  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

}
