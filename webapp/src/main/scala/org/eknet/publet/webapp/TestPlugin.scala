package org.eknet.publet.webapp

import akka.actor.{Props, ActorSystem, ActorRef}
import org.eknet.publet.content._
import scala.concurrent.Future
import java.text.DateFormat
import java.util.Date
import org.eknet.publet.actor.docroot.ActorResource
import org.eknet.publet.content.Source.StringSource
import org.eknet.publet.content.Resource.SimpleContent
import scala.Some
import org.slf4j.LoggerFactory
import org.eknet.publet.actor.{Publet, Plugin}
import org.eknet.publet.gitr.GitrPlugin

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 10.05.13 23:15
 */
object TestPlugin extends Plugin {
  val time = System.currentTimeMillis()

  val log = LoggerFactory.getLogger(getClass)

  def name = "test-plugin"

  def dependsOn = Set(GitrPlugin.name)

  def load(ref: ActorRef, system: ActorSystem) = {
    val testRes = system.actorOf(Props[TestActorResource], name = "testactorresource")
    val publet = Publet(system)
    publet.documentRoot.send(dr => dr.mount("/dyna", container(testRes)))
    publet.documentRoot.send(dr => dr.mount("/wiki", publet.partitionFactory().create("git:///wiki/eike")))

    system.actorOf(Props[TestActor], name = "TestActor")

    Future.successful("Ok")
  }

  def container(testRes: ActorRef) = new SimplePartition(Seq(
    SimpleContent("message.scala", StringSource(" case class Mod(name: String)\n " +
      "val db = context.extension(GraphdbExt).getOrCreate(\"testdb\")\n " +
      "Some(Source.html(\"<h2>Scala Compiled: </h2><p> \" + context.path.absoluteString + \"</p>\\n <p>\"+db+\"</p>\")) ", ContentType.`text/x-scala`, Some(time))),
    new DynamicContent {
      def name = "test.html"
      def create(params: Map[String, String]) = Future.successful(Some {
        val name = params.get("name").getOrElse("foreigner")
        Source.html {
          <html>
            <head>
              <link rel="stylesheet" href="/publet/assets/compressed/css/allof.css?name=default"></link>
              <script type="text/javascript" src="/publet/assets/compressed/js/allof.js?name=default"></script>
            </head>
            <body>
              <div id="content" class="container">
                <div class="page-header">
                  <h1>Welcome { name } to a dynamic world! Its now {DateFormat.getDateTimeInstance.format(new Date())}</h1>
                </div>
                <p class="alert alert-info"><bold>Hey!</bold> Why is nothing being displayed???!!! <a class="btn"><i class="icon-user"></i> Login</a></p>
                <pre><code class="scala"> class MyWebExtension {{
                  def executionContext: MessageDispatcher = context.dispatcher
                }}</code></pre>
                <p>or</p>
                <pre><code>public static void main() {{ System.out.println("Test"); }}</code></pre>
              </div>
            </body>
          </html>
        }
      })
    },
    new SimpleContent("help.md", Source.markdown(
      """
        |<div class="page-header"><h1>Help me with</h1></div>
        |
        |Some text here. I hope [Scalate](http://scalate.fusesource.net) is doing fine now.
        |
        |> Citation needed
        |
        |See this example code
        |
        |```scala
        |val f: BlueprintsGraph => Any = g => g.addVertex()
        |```
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
