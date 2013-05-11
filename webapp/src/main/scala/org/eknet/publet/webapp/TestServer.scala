package org.eknet.publet.webapp

import spray.can.server.SprayCanHttpServerApp
import akka.actor.{Props, ActorRef, ActorSystem}
import com.typesafe.config.ConfigFactory
import org.eknet.publet.webapp.WorkActor.Ready

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 30.04.13 22:16
 */
object TestServer extends App with SprayCanHttpServerApp {

  val cfg =
    """
      | publet.mounts = [
      |#    {
      |#      # mount directories below $workdir
      |#      uri = "var://maincontent"
      |#      paths = [ "/main", "/othermain" ],
      |#    }
      |#
      |    {
      |      # mount temp directories below $tempdir
      |      uri = "tmp://testpartition"
      |      paths = [ "/temp", "/main" ]
      |    }
      |#
      |#   {
      |#      # mount resources within the class path
      |#      uri = "classpath://org/some/package"
      |#      paths = [ "/other/resources", "/path2" ]
      |#    }
      | ]
      |
    """.stripMargin

  val config = ConfigFactory.load()

  override lazy val system = ActorSystem("publet-server", config)

  val handler = system.actorOf(Props[WorkActor], name = "publet-http")
  val server = newHttpServer(handler, name = "spray-http-server")
  handler ! Ready(server)
}
