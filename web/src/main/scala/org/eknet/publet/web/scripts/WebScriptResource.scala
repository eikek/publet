package org.eknet.publet.web.scripts

import org.eknet.publet.web.WebContext
import org.eknet.publet.web.util.{Request, Key}
import org.eknet.publet.engine.scala.{ScriptResource, ScalaScript}
import org.eknet.publet.vfs.ResourceName

/** A resource that executes the given script on access. The result
 * is cached as attribute inside the request.
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 26.04.12 19:28
 */
class WebScriptResource(name: ResourceName, script: ScalaScript)
    extends ScriptResource(name, script) {

  override def evaluate = {
    val ctx = WebContext()
    val scriptResultKey = Key("publet.web.script.Result", {
      case Request => super.evaluate
    })

    ctx(scriptResultKey).get
  }

}
