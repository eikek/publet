package org.eknet.publet.engine.scalascript

import org.eknet.publet.engine.PubletEngine
import org.eknet.publet.vfs.{Path, ContentType, Content}
import com.twitter.util.Eval
import scala.Some

/**
 * Compiles scala classes that extend `ScalaScript` and feeds the outcome
 * into the given engine.
 *
 * @author eike.kettner@gmail.com
 * @since 10.04.12 17:06
 */
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

  def process(path: Path, data: Seq[Content], target: ContentType) = {
    data find (_.contentType == ContentType.scal) match {
      case Some(c) => engine.process(path, Seq(eval(c)), target)
      case None => throw new RuntimeException("no scala script content found")
    }
  }

  def eval(content: Content): Content = {
    val r = eval.apply[Any](boxedScript(content.contentAsString), false)

    if (r.isInstanceOf[Content]) r.asInstanceOf[Content]
    else Content(r.toString, ContentType.html)
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
