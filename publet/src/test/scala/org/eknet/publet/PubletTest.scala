package org.eknet.publet

import engine.{PassThrough, DefaultConverterEngine}
import io.Source
import org.eknet.publet.source.FilesystemSource


/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 28.03.12 22:17
 */
object PubletTest {

  def main(args: Array[String]) {
    val publ = Publet()
    publ.addSource(new FilesystemSource("/tmp/publet"))
    publ.mount("/*", new DefaultConverterEngine)
    publ.mount("/pamflet/*", new PassThrough)

    val uri = Uri("local:///test/hello.txt")
    publ.process(uri) match {
      case Left(x) => throw x
      case Right(x) => x match {
        case None => println("Not found: " + uri)
        case Some(data) => println(data.contentType); Source.createBufferedSource(data.content).getLines().foreach(println)
      }
    }
  }
}
