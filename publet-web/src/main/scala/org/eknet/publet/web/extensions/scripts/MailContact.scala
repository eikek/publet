package org.eknet.publet.web.extensions.scripts

import org.eknet.publet.engine.scalascript.ScalaScript
import org.eknet.publet.web.WebContext
import org.eknet.publet.web.Key
import ScalaScript._

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 15.04.12 23:16
 */
object MailContact extends ScalaScript {

  import org.eknet.publet.web.extensions.MailSupport._

  def contactForm() = makeHtml {
    <h2>Kontakt</h2>
    <div class="formSubmitResponse"></div>
    <form class="ym-form linearize-form" action="testscript.html">
      <input type="hidden" name="a" value="eval"/>
      <div class="ym-fbox-text">
        <label for="from">Von (Email)<sup class="ym-required">*</sup></label>
        <input type="text" name="from" id="from" size="20" required="required"/>
      </div>
      <div class="ym-fbox-text">
        <label for="message">Nachricht<sup class="ym-required">*</sup></label>
        <textarea name="message" rows="10" required="required"></textarea>
      </div>
      <div class="ym-fbox-text">
        <label for="captcha">Captcha<sup class="ym-required">*</sup></label>
        <input type="text" name="captcha" id="captcha" size="20" required="required"/>
      </div>
      <div class="ym-fbox-select">
        <img alt="captcha" src="/.publets/scripts/captcha.png?col1=ffffff&amp;col2=ffffff&amp;fgcol=000000&amp;captchaParam=contactCaptcha" />
      </div>
      <button class="ym-button ym-email publetAjaxSubmit">Senden</button>
    </form>
  }

  def sendMail(from: String, msg: String) {
    val ctx = WebContext()
    import ctx._

    //TODO get this from config file
    val sender = service(senderKey("smtp.server.com", -1, "username", "password"))
    val mail = newMail(from);
    mail.setText(msg)
    mail.setSubject("Kontaktformular")
    mail.addTo("eike@eknet.org")
    sender.send(mail)
  }


  def serve() = {
    val ctx = WebContext()
    import ctx._

    val from = parameter("from")
    val msg = parameter("message")
    val captcha = parameter("captcha")
    val cstr = session.remove(Key[String]("contactCaptcha"))

    if (from.isDefined && msg.isDefined && captcha.isDefined) {
      if (cstr == captcha) {
        sendMail(from.get, msg.get)
        makeHtml(<p class="box success">Message sent! <a href="">Reload</a></p>)
      } else {
        makeHtml(<p class="box error">Captcha does not match! <a href="">Reload</a></p>)
      }
    } else {
      contactForm()
    }
  }

}
