package org.eknet.publet.web.scripts

import org.eknet.publet.engine.scalascript.{ScriptResource, ScalaScript}
import org.eknet.publet.web.WebContext
import org.eknet.publet.vfs.{ContentType, Path}
import org.eknet.publet.web.util.{Request, Key}

/** A resource that executes the given script on access. The result
 * is cached as attribute inside the request.
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 26.04.12 19:28
 */
class WebScriptResource(path: Path, script: ScalaScript, contentType: ContentType)
    extends ScriptResource(path, script, contentType) {

  override def evaluate = {
    val ctx = WebContext()
    val scriptResultKey = Key("publet.web.script.Result", {
      case Request => super.evaluate
    })

    ctx(scriptResultKey).get
  }

}
