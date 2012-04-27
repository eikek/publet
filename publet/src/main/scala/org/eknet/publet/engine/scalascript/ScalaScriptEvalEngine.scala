package org.eknet.publet.engine.scalascript

import org.eknet.publet.engine.PubletEngine
import com.twitter.util.Eval
import scala.Some
import org.eknet.publet.vfs._

/**
 * Compiles scala classes that extend `ScalaScript` and feeds the outcome
 * into the given engine.
 *
 * @author eike.kettner@gmail.com
 * @since 10.04.12 17:06
 */
@deprecated("use the one from scala-script module", "always")
class ScalaScriptEvalEngine(val name: Symbol, engine: PubletEngine) extends PubletEngine {

//  val tmpdir = new File(System.getProperty("java.io.tmpdir"))
//  val out = Some {
//    val f = new File(tmpdir, "publetscriptout")
//    if (f.exists() && !f.isDirectory) sys.error("Not a directory: "+ f)
//    else if (!f.exists() && !f.mkdirs()) sys.error("Cannot create outputdir")
//    f
//  }

  // with `None` class files are not written but cached in memory
  val eval = new Eval(None)

  def process(data: Seq[ContentResource], target: ContentType) = {
    data find (_.contentType == ContentType.scal) match {
      case Some(c) => engine.process(Seq(eval(c)), target)
      case None => throw new RuntimeException("no scala script content found")
    }
  }

  def eval(content: ContentResource): ContentResource = {
    val r = eval.apply[Any](boxedScript(content.contentAsString), false)

    if (r.isInstanceOf[Content]) new CompositeContentResource(content, r.asInstanceOf[Content])
    else new CompositeContentResource(content, Content(r.toString, ContentType.html))
  }

  def boxedScript(script: String): String = {
    importPackages.map("import " + _).mkString("\n") + "\n\n"+ script
  }

  def importPackages: List[String] = List(
    "org.eknet.publet.engine.scalascript.ScalaScript._",
    "org.eknet.publet.resource.ContentType._",
    "org.eknet.publet.resource.Content",
    "org.eknet.publet.resource.NodeContent"
  )

}
