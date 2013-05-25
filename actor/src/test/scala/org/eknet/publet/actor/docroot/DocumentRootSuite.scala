package org.eknet.publet.actor.docroot

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import akka.util.Timeout
import akka.testkit.TestActorRef
import org.eknet.publet.content._
import scala.util.Try
import akka.actor.ActorSystem
import scala.util.Success
import scala.Some
import org.eknet.publet.actor.messages._

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 07.05.13 19:54
 */
class DocumentRootSuite extends FunSuite with ShouldMatchers {

  import scala.concurrent.duration._
  import akka.pattern.ask
  implicit val timeout: Timeout = 5.seconds
  implicit val system = ActorSystem("testsystem")

  val testPartition = new SimplePartition(Seq(
    Resource.EmptyContent("text1.txt"),
    Resource.EmptyContent("text2.pdf"),
    Resource.EmptyContent("image.png")
  ))

  test ("mount and create partition") {
//    val docroot = TestActorRef[DocumentRoot]("documentroot")
//    val partRef1 = TestActorRef(PartitionActor(testPartition), name = "cms")
//    val f = docroot ? Mount(partRef1, Set("/a", "/b"))
//    val Success(Success(seq)) = f.mapTo[Try[Seq[Path]]].value.get
//    seq should be (Seq(Path("/a"), Path("/b")))
//
//    val ref = system.actorFor("akka://testsystem/user/cms")
//    ref should be (partRef1)
//
//    val resolved = docroot.underlyingActor.resolve("/a/text2.pdf").t.get._2
//    resolved should be (ref)
//
//    val f2 = docroot ? Find("/a/text2.pdf")
//    val Success(Some(resource)) = f2.mapTo[Option[Resource]].value.get
//    resource.name should be (Name("text2.pdf"))
//
//    val f3 = docroot ? Find("/z/d/f.txt")
//    f3.value.get should be (Success(None))
  }

}

