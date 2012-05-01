package org.eknet.publet.engine.scala

import org.eknet.publet.Publet
import org.eknet.publet.vfs.{Path, ContentResource}
import tools.nsc.io.VirtualDirectory

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 27.04.12 21:00
 */
class DefaultPubletCompiler(val publet: Publet,
                            val pathPrefix: String,
                            imports: List[String]) extends PubletCompiler {


  val compiler = new ScriptCompiler(new VirtualDirectory("(memory)", None), imports)

  def evaluate(path: Path, resource: ContentResource) = {
    val miniProject = MiniProject.find(path, publet, pathPrefix)
    val script = compiler.scriptLoader(miniProject, path, resource)
    Some(script)
  }

}
