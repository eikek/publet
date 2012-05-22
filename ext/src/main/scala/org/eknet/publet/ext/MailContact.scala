package org.eknet.publet.ext

import org.eknet.publet.engine.scala.ScalaScript
import ScalaScript._
import org.eknet.publet.ext.MailSupport._
import org.eknet.publet.web.util.RenderUtils._
import org.eknet.publet.web.{PubletWeb, Config, PubletWebContext}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 15.04.12 23:16
 */
object MailContact extends ScalaScript {

  val formTemplate = "/publet/ext/includes/templates/_contact.jade"
  val actionUrl = PubletWeb.servletContext.getContextPath + "/publet/ext/scripts/contact.json"

  def serve() = {
    val ctx = PubletWebContext
    import ctx._

    if (Config("smtp.host").isEmpty || Config("defaultReceiver").isEmpty) {
      renderMessage("Mailer not configured", "Mailer not configured! Sorry, the contact form is not working.", "error")
    } else {
      val from = param("from")
      val msg = param("message")
      val invisible = param("text").exists(!_.isEmpty)
      if (from.isDefined && msg.isDefined && !invisible) {
        newMail(from.get)
          .to(Config("defaultReceiver").get)
          .subject("Kontaktformular")
          .text(msg.get)
          .send()
        makeJson(Map("success"->true, "message"->"Mail successfully sent."))
      } else {
        renderTemplate(formTemplate, Map[String, Any]("actionUrl"->actionUrl))
      }
    }
  }


}
