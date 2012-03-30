package org.eknet.publet

import engine.{PassThrough, DefaultConverterEngine}
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

    val conveng = new DefaultConverterEngine
    val md2html = (data:Page) => { println("md->html"); data }
    val html2text = (data:Page) => { println("html->text"); data }
    val html2pdf = (data:Page) => { println("html->pdf"); data }
    val pdf2text = (data:Page) => { println("pdf->text"); data }

    conveng.addConverter(ContentType.markdown, ContentType.html, md2html)
    conveng.addConverter(ContentType.html, ContentType.text, html2text)
    conveng.addConverter(ContentType.html, ContentType.pdf, html2pdf)
    conveng.addConverter(ContentType.pdf, ContentType.text, pdf2text)

    publ.register("/*", conveng)
    publ.register("/pamflet/*", PassThrough)

    val path = Path("/test/hello.txt")
    publ.process(path) match {
      case Left(x) => throw x
      case Right(x) => x match {
        case None => println("Not found: " + path)
        case Some(data) => println(data.contentType); Source.createBufferedSource(data.content).getLines().foreach(println)
      }
    }
  }
}
