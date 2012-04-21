package org.eknet.publet.web.extensions.scripts

import org.eknet.publet.engine.scalascript.ScalaScript
import ScalaScript._
import org.eknet.publet.web.{Config, WebContext, Key}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 15.04.12 23:16
 */
object MailContact extends ScalaScript {

  import org.eknet.publet.web.extensions.MailSupport._

  def contactForm() = makeHtml {
    <h2>Kontakt</h2>
    <div class="formSubmitResponse"></div>
    <form class="ym-form linearize-form" action="">
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
  }//TODO the path to the captcha servlet must be universal!
   //TODO supply i18n possiblity

  def serve() = {
    val ctx = WebContext()
    import ctx._

    if (Config("smtp.host").isEmpty || Config("defaultReceiver").isEmpty) {
      makeHtml(<p class="box error">No SMTP configuration and/or defaultReceiver provided.</p>)
    } else {
      val from = parameter("from")
      val msg = parameter("message")
      val captcha = parameter("captcha")
      val cstr = ctx(Key[String]("contactCaptcha"))

      if (from.isDefined && msg.isDefined && captcha.isDefined) {
        if (cstr == captcha) {
          newMail(from.get)
            .to(Config("defaultReceiver").get)
            .subject("Kontaktformular")
            .text(msg.get)
            .send()
          makeHtml(<p class="box success">Message sent! <a href="">Reload</a></p>)
        } else {
          makeHtml(<p class="box error">Captcha does not match! <a href="">Reload</a></p>)
        }
      } else {
        contactForm()
      }
    }
  }

}
