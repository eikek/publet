package org.eknet.publet

import engine.PassThrough
import java.net.URI
import source.FilesystemSource
import io.Source

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 28.03.12 22:17
 */
object  PubletTest {

  def main(args: Array[String]) {
    val publ = Publet()
    publ.addSource(new FilesystemSource("/tmp/publet"))
    publ.addEngine(".*".r, new PassThrough)
    
    val data = publ.process(URI.create("local:///hello.txt")).right.get.get
    val lines = Source.createBufferedSource(data.content).getLines()
    lines.foreach(println)

  }
}
