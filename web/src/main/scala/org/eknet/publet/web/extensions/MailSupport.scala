package org.eknet.publet.web.extensions

import javax.mail.internet.InternetAddress
import org.eknet.squaremail._
import org.eknet.publet.web.{WebContext, Config}
import org.eknet.publet.web.util.{Context, Key}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 14.04.12 17:49
 */
object MailSupport {

  import org.eknet.squaremail.Messages._
  
  implicit def email(str: String) = parseAddress(str)
  implicit def str2CharArray(str: String): Array[Char] = str.toCharArray
  implicit def mail2EasyMail(m: MailMessage) = new EasyMail(m)

  def newMail(from: InternetAddress) = new DefaulMailMessage(from)

  def sessionFactory(host: String, port: Int = -1, user: String, password: Array[Char]) = 
    new DefaultSessionFactory(host, port, user, password)

  def sender(host: String, port: Int = -1, user: String, password: Array[Char]): MailSender =
    new DefaultMailSender(sessionFactory(host, port, user, password))
  
  def senderKey(host: String, port: Int = -1, user: String, password: Array[Char]): Key[MailSender] = Key(host+port+user, {
    case Context => sender(host, port, user, password)
  })

  /**Creates a key to obtain the mail service that is
   * configured from the config file. The value `smtp.host`
   * is mandatory in the config file.
   *
   * @return
   */
  def senderKey(): Key[MailSender] = senderKey(Config("smtp.host").get,
    Config("smtp.port").getOrElse("-1").toInt,
    Config("smtp.username").getOrElse(""),
    Config("smtp.password").getOrElse("").toCharArray)

  class EasyMail(mail: MailMessage) {
    def send() {
      val ctx = WebContext()
      ctx.service(senderKey()).send(mail)
    }

    def to(em: InternetAddress) = {
      mail.addTo(em)
      this
    }
    def subject(s:String) = {
      mail.setSubject(s)
      this
    }
    def text(t:String) = {
      mail.setText(t)
      this
    }
    def html(t:String) = {
      mail.setHtmlText(t)
      this
    }
  }

}
