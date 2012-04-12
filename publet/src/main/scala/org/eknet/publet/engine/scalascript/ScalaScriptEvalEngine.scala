package org.eknet.publet.engine.scalascript

import org.eknet.publet.engine.PubletEngine
import org.eknet.publet.Path
import org.eknet.publet.resource.{ContentType, Content}
import com.twitter.util.Eval

/**
 * Compiles scala classes that extend $ScalaScript and feeds the outcome
 * into the given engine.
 *
 * @author eike.kettner@gmail.com
 * @since 10.04.12 17:06
 */
class ScalaScriptEvalEngine(val name: Symbol, engine: PubletEngine) extends PubletEngine {

  def process(path: Path, data: Seq[Content], target: ContentType) = {
    data find (_.contentType == ContentType.scal) match {
      case Some(c) => engine.process(path, Seq(eval(c)), target)
      case None => Left(new RuntimeException("no scala script content found"))
    }
  }

  def eval(content: Content): Content = {
    new Eval(None).apply(boxedScript(content.contentAsString))
  }

  def boxedScript(script: String): String = {
    val body = "class\\s+([^\\s]+)\\s+".r.findFirstMatchIn(script) match {
      case Some(m) => "\n\n%s\n\n new " + m.group(1) + "().serve()"
      case None => "\n\nnew %s.serve()"
    }

    val templ = importPackages.map("import " + _).mkString("\n") + body
    String.format(templ, script)
  }

  def importPackages: List[String] = List(
    "org.eknet.publet.engine.scalascript.ScalaScript",
    "org.eknet.publet.resource.ContentType._",
    "org.eknet.publet.resource.Content"
  )

  lazy val testScriptStr = """
  class TestScript extends ScalaScript {
    def serve() = Content("<h1>Yeah!</h1>", html)
  }
  """

  lazy val testScript2 = """
  ScalaScript {
    def serve() = Content("<h2>Yes</h2>", html)
  }
  """

  lazy val testScript3 = """
  Content("<h2>And again!</h2>", html)
  """
}
