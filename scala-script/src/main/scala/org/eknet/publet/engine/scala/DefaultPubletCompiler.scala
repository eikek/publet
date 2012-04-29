package org.eknet.publet.engine.scala

import org.eknet.publet.Publet
import org.eknet.publet.vfs.{Path, ContentResource}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 27.04.12 21:00
 */
class DefaultPubletCompiler(val publet: Publet,
                            val pathPrefix: String,
                            imports: List[String]) extends PubletCompiler {


  def evaluate(path: Path, resource: ContentResource) = {
    val  comp = findCompiler(path, resource)
    val script = comp.loadScalaScriptClass(path, resource)
    Some(script)
  }

  def findCompiler(path: Path, resource: ContentResource): ScriptCompiler = {
    val miniProject = MiniProject.find(path, publet, pathPrefix)

    ScriptCompiler(None, miniProject, imports)
  }
}
