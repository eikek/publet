package org.eknet.publet.engine.scala

import org.eknet.publet.Publet
import org.eknet.publet.vfs.ContentResource

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 27.04.12 21:00
 */
class DefaultPubletCompiler(val publet: Publet,
                            val pathPrefix: String,
                            imports: List[String]) extends PubletCompiler {

  def evaluate(resource: ContentResource) = {
    val  comp = findCompiler(resource)
    val script = comp.loadScalaScriptClass(resource)
    script.serve()
  }

  private val compiler = ScriptCompiler(None, None, None, imports)

  def findCompiler(resource: ContentResource): ScriptCompiler = {
    val miniProject = MiniProject.find(resource.path, publet, pathPrefix)
    val rootProject = MiniProject.rootProject(pathPrefix, publet)

//    ScriptCompiler(None, miniProject, rootProject, imports)
    compiler
  }
}
