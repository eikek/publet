package org.eknet.publet.actor

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import akka.testkit.TestActorRef
import org.eknet.publet.content.{Glob, Path, Engine}
import akka.actor.{ActorSystem, ActorRef}
import akka.pattern.ask
import scala.util.Success
import akka.util.Timeout
import scala.concurrent.duration._
import org.eknet.publet.actor.convert.Converter
import org.eknet.publet.actor.messages._

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 04.05.13 16:27
 */
class EngineRegistrySuite extends FunSuite with ShouldMatchers {

  implicit val timeout: Timeout = 5.seconds
  implicit val system = ActorSystem()
  val engine: Engine = { case (c, t) => c }

  test ("register, unregister and get") {
    val ref = TestActorRef[Converter]
    val dummyRef = TestActorRef[DummyActor]("engine")
    val createF = ref ? Register(dummyRef, Set("/ab/**"))
    val Success(Success(pattern: Seq[_])) = createF.value.get
    pattern should be (Seq(Glob("/ab/**")))

    ref.underlyingActor.findName("/ab/test/com").get should have size (1)
    ref.underlyingActor.findName("/ab/test/com").get.head should be (dummyRef)
    ref.underlyingActor.find("/ab/test/com") should be (Some(dummyRef))

    val future = (ref ? GetEngine("/ab/test/com")).mapTo[Option[ActorRef]]
    val Success(result: Option[ActorRef]) = future.value.get
    result.get should be(dummyRef)

    ref ! Unregister(Set("/ab/**"))
    ref.underlyingActor.findName("/ab/test/com") should be (None)
    ref.underlyingActor.find("/ab/test/com") should be (None)
  }

}
