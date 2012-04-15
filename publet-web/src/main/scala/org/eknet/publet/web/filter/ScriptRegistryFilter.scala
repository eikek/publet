package org.eknet.publet.web.filter

import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import org.eknet.publet.engine.scalascript.ScalaScript
import org.eknet.publet.Path
import org.eknet.publet.web.WebContext
import org.eknet.publet.web.extensions.scripts.{MailContact, CaptchaScript}

/** Executes precompiled scripts.
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 15.04.12 15:52
 */
object ScriptRegistryFilter extends Filter with PageWriter {

  private val scripts = Map[String, ScalaScript](
    "captcha.png" -> CaptchaScript,
    "mailcontact.html" -> MailContact
  );

  private val prefix = Path("/.publets/scripts")


  def handle(req: HttpServletRequest, resp: HttpServletResponse) = {
    val path = WebContext().requestPath
    scripts.get(path.segments.last) match {
      case Some(scr) if (path == (prefix / path.segments.last)) => {
        writePage(Some(scr.serve()), path, req, resp)
        true
      }
      case _ => false
    }
  }
}
