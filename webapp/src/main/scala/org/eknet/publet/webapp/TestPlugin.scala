package org.eknet.publet.webapp

import akka.actor.{Props, ActorSystem, ActorRef}
import org.eknet.publet.content._
import scala.concurrent.Future
import java.text.DateFormat
import java.util.Date
import org.eknet.publet.actor.docroot.{PartitionActor, ActorResource}
import org.eknet.publet.content.Source.XmlSource
import org.eknet.publet.content.Source.StringSource
import org.eknet.publet.content.Resource.SimpleContent
import scala.Some
import org.slf4j.LoggerFactory
import org.eknet.publet.actor.messages.{Mount, MountUri}
import java.net.URI
import akka.util.Timeout
import org.eknet.publet.actor.{Publet, Plugin}
import org.eknet.publet.gitr.GitrPlugin

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 10.05.13 23:15
 */
object TestPlugin extends Plugin {

  val log = LoggerFactory.getLogger(getClass)

  def name = "test-plugin"

  def dependsOn = Set(GitrPlugin.name)

  def load(ref: ActorRef, system: ActorSystem) = {
    val testRes = system.actorOf(Props[TestActorResource], name = "testactorresource")
    val actor = system.actorOf(PartitionActor(container(testRes)), name = "dyntest-container")
    system.actorOf(Props[TestActor])

    import akka.pattern.ask
    import scala.concurrent.duration._
    import system.dispatcher
    implicit val timeout: Timeout = 15.seconds

    Publet(system).documentRoot.mount("/some/linked", Publet(system).partitionFactory("tmp:///testparty"))

    val f1 = ref ? MountUri(new URI("git:///wiki/eike"), Set("/wiki"))
    val f2 = ref ? Mount(actor, Set("/dyna"))
    Future.sequence(Seq(f1, f2))
  }


  def container(testRes: ActorRef) = new SimplePartition(Seq(
    new DynamicContent {
      def name = "test.html"
      def create(params: Map[String, String]) = Future.successful(Some {
        val name = params.get("name").getOrElse("foreigner")
        Source.html(<html><body><h1>Welcome { name } to a dynamic world! Its now {DateFormat.getDateTimeInstance.format(new Date())}</h1></body></html>)
      })
    },
    new SimpleContent("help.md", Source.markdown(
      """
        |# Help me with
        |
        |Some text here. I hope [Scalate](http://scalate.fusesource.net) is doing fine now.
        |
        |> Citation needed
        |
        |Bye
      """.stripMargin)
    ),
    ActorResource("actor.md", testRes)
  ))

  val myengine: Engine = {
    case (source @ Source(ContentType.`text/x-markdown`), ContentType(_, "html", false)) => {
      log.info(s">>>> ---- converting ${source.contentType} to text/html...")
      StringSource("<html><body><pre>"+ io.Source.fromInputStream(source.inputStream).getLines().mkString("\n")+"</pre></body></html>")
        .withType(ContentType.`text/html`)
    }
  }
}
