package org.eknet.publet.webapp.assets

import java.io._
import com.yahoo.platform.yui.compressor.{CssCompressor, JavaScriptCompressor}
import org.mozilla.javascript.{EvaluatorException, ErrorReporter}
import org.eknet.publet.content.{Name, ContentType, Content}
import org.eknet.publet.content.Resource.SimpleContent
import org.eknet.publet.content.Source.ByteArraySource
import com.typesafe.scalalogging.slf4j.Logging
import org.parboiled.common.FileUtils

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 16.05.13 18:25
 */
object compress extends Logging {

  class ListEnumeration[A](e: Iterable[A]) extends java.util.Enumeration[A] {
    val iter = e.iterator
    def hasMoreElements = iter.hasNext
    def nextElement() = iter.next()
  }
  implicit def toEnumeration[A](e: Iterable[A]) = new ListEnumeration[A](e)

  object CompressType extends Enumeration {

    private val jsFilter: Asset => Boolean = _.resource.contentType.subType == "javascript"
    private val cssFilter: Asset => Boolean = _.resource.contentType.subType == "css"
    case class Type(name: String, filter: Asset => Boolean) extends Val

    val js = Type("js", jsFilter)
    val css = Type("css", cssFilter)
  }

  def compressJs(in: InputStream, out: OutputStream) {
    val compressor = new JavaScriptCompressor(new InputStreamReader(in, "UTF-8"), new ErrorReporter {
      def warning(message: String, sourceName: String, line: Int, lineSource: String, lineOffset: Int) {
        logger.warn(message)
      }
      def error(message: String, sourceName: String, line: Int, lineSource: String, lineOffset: Int) {
        logger.error(message)
      }
      def runtimeError(message: String, sourceName: String, line: Int, lineSource: String, lineOffset: Int) = {
        logger.error(message)
        new EvaluatorException(message, sourceName, line, lineSource, lineOffset)
      }
    })
    val writer = new BufferedWriter(new OutputStreamWriter(out))
    compressor.compress(writer, -1, true, false, false, false)
    writer.flush()
  }

  def compressCss(in: InputStream, out: OutputStream) {
    val compressor = new CssCompressor(new InputStreamReader(in, "UTF-8"))
    val writer = new BufferedWriter(new OutputStreamWriter(out))
    compressor.compress(writer, -1)
    writer.flush()
  }

  def compress(c: Content) = {
    val bout = new ByteArrayOutputStream()
    c.contentType match {
      case ContentType(_, "javascript", false) => {
        compressJs(c.inputStream, bout)
        SimpleContent(c.name, ByteArraySource(bout.toByteArray, c.contentType))
      }
      case ContentType("text", "css", false) => {
        compressCss(c.inputStream,bout)
        SimpleContent(c.name, ByteArraySource(bout.toByteArray, c.contentType))
      }
      case _ => c
    }
  }

  def createCompressedFile(name: Name, assets: List[Asset]) = {
    val ins = assets withFilter(_.merge) map { a =>
      if (a.compress) compress(a.resource).inputStream else a.resource.inputStream
    }
    val newline = () => new ByteArrayInputStream("\n\n".getBytes("UTF-8"))
    val in = new SequenceInputStream(ins.flatMap(i => List(i, newline())))
    SimpleContent(name, ByteArraySource(FileUtils.readAllBytes(in), name.contentType.get))
  }

}
