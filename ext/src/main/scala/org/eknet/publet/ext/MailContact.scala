package org.eknet.publet.ext

import org.eknet.publet.engine.scala.ScalaScript
import ScalaScript._
import org.eknet.publet.web.util.Key
import org.eknet.publet.ext.MailSupport._
import org.eknet.publet.vfs.Path
import org.eknet.publet.web.{Config, PubletWebContext, PubletWeb}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 15.04.12 23:16
 */
object MailContact extends ScalaScript {

  def actionUrl = PubletWeb.servletContext.getContextPath+ "/publet/ext/scripts/mailcontact.html"
  def captchaUrl = PubletWeb.servletContext.getContextPath+ "/publet/ext/scripts/captcha.png"

  def contactForm() = makeSsp {
    <h2>Kontakt</h2>
      <div class="formSubmitResponse"></div>
      <form class="ym-form linearize-form" action={ actionUrl }>
        <input type="hidden" name="a" value="include"/>
        <div class="ym-fbox-text">
          <label for="from">Von (Email)
            <sup class="ym-required">*</sup>
          </label>
            <input type="text" name="from" id="from" size="20" required="required"/>
        </div>
        <div class="ym-fbox-text">
          <label for="message">Nachricht
            <sup class="ym-required">*</sup>
          </label>
          <textarea name="message" rows="10" required="required"></textarea>
        </div>
        <div class="ym-fbox-text">
          <label for="captcha">Captcha
            <sup class="ym-required">*</sup>
          </label>
            <input type="text" name="captcha" id="captcha" size="20" required="required"/>
        </div>
        <div class="ym-fbox-select">
          <img alt="captcha" src={ captchaUrl + "?col1=ffffff&col2=ffffff&fgcol=000000&captchaParam=contactCaptcha"} />
    </div>
    <button class="ym-button ym-email publetAjaxSubmit">Senden</button>
    </form>
  } //TODO the path to the captcha servlet must be universal!
  //TODO supply i18n possiblity

  def serve() = {
    val ctx = PubletWebContext
    import ctx._

    if (Config("smtp.host").isEmpty || Config("defaultReceiver").isEmpty) {
      makeSsp(<p class="box error">No SMTP configuration and/or defaultReceiver provided.</p>)
    } else {
      val from = param("from")
      val msg = param("message")
      val captcha = param("captcha")
      val cstr = ctx.sessionMap.remove(Key[String]("contactCaptcha"))

      if (from.isDefined && msg.isDefined && captcha.isDefined) {
        if (cstr == captcha) {
          newMail(from.get)
            .to(Config("defaultReceiver").get)
            .subject("Kontaktformular")
            .text(msg.get)
            .send()
          makeSsp(<p class="box success">Message sent!
            <a href=" ">Reload</a>
          </p>)
        } else {
          makeSsp(<p class="box error">Captcha does not match!
            <a href=" ">Reload</a>
          </p>)
        }
      } else {
        contactForm()
      }
    }
  }

}
