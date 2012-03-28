package org.eknet.publet

import java.net.URI
import io.Source
import org.eknet.publet.source.FilesystemSource
import org.eknet.publet.engine.PassThrough

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 28.03.12 22:17
 */
object PubletTest {

  def main(args: Array[String]) {
    val publ = Publet()
    publ.addSource(new FilesystemSource("/tmp/publet"))
    publ.addEngine("""^/.*""".r, new PassThrough)

    val uri = URI.create("local:///hello.txt")

    publ.process(uri) match {
      case Left(x) => throw x
      case Right(x) => x match {
        case None => println("Not found: " + uri)
        case Some(data) => Source.createBufferedSource(data.content).getLines().foreach(println)
      }
    }
  }
}
