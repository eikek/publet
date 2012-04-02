package org.eknet.publet

import engine._
import org.eknet.publet.ContentType._
import io.Source
import org.eknet.publet.source.FilesystemPartition


/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 28.03.12 22:17
 */
object PubletTest {

  def main(args: Array[String]) {
    val publ = Publet()
    publ.mount(Path("/"), new FilesystemPartition("/tmp/publet"))

    val conveng = ConverterEngine()
    val md2html = (data:Content) => { println("md->html"); data }
    val html2text = (data:Content) => { println("html->text"); data }
    val html2pdf = (data:Content) => { println("html->pdf"); data }
    val pdf2text = (data:Content) => { println("pdf->text"); data }

    conveng.addConverter((markdown -> html), KnockoffConverter)
    conveng.addConverter((html -> text), html2text)
    conveng.addConverter((html -> pdf), html2pdf)
    conveng.addConverter((pdf -> text), pdf2text)

    publ.register("/*", conveng)
    publ.register("/pamflet/*", PassThrough)

    val path = Path("/test/story.html")
    publ.process(path) match {
      case Left(x) => throw x
      case Right(x) => x match {
        case None => println("Not found: " + path)
        case Some(data) => println(data.contentType); Source.createBufferedSource(data.content).getLines().foreach(println)
      }
    }
  }
}
