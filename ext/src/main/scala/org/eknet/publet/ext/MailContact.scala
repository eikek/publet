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

  lazy val formTemplate = "/publet/ext/includes/templates/_contact.jade"
  lazy val actionUrl = "/publet/ext/scripts/contact.json"

  def serve() = {
    val ctx = PubletWebContext
    import ctx._

    if (Config("smtp.host").isEmpty || Config("defaultReceiver").isEmpty) {
      renderMessage("Mailer not configured", "Mailer not configured! Sorry, the contact form is not working.", "error")
    } else {
      val appName = PubletWeb.publetSettings("applicationName").map("["+ _ +"] ").getOrElse("")
      val from = param("from")
      val msg = param("message")
      val invisible = param("text").exists(!_.isEmpty)
      if (from.isDefined && msg.isDefined && !invisible) {
        newMail(from.get)
          .to(Config("defaultReceiver").get)
          .subject(appName+ "Contact Form")
          .text(msg.get)
          .send()
        makeJson(Map("success"->true, "message"->"Mail successfully sent."))
      } else {
        renderTemplate(formTemplate, Map[String, Any]("actionUrl"->actionUrl))
      }
    }
  }


}
