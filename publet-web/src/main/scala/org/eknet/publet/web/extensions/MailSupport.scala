package org.eknet.publet.web.extensions

import javax.mail.internet.InternetAddress
import org.eknet.squaremail.{MailSender, DefaultSessionFactory, DefaultMailSender, DefaulMailMessage}
import org.eknet.publet.web.{Context, Key}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 14.04.12 17:49
 */
object MailSupport {

  import org.eknet.squaremail.Messages._
  
  implicit def email(str: String) = parseAddress(str)
  implicit def str2CharArray(str: String): Array[Char] = str.toCharArray

  def newMail(from: InternetAddress) = new DefaulMailMessage(from)

  def sessionFactory(host: String, port: Int = -1, user: String, password: Array[Char]) = 
    new DefaultSessionFactory(host, port, user, password)

  def sender(host: String, port: Int = -1, user: String, password: Array[Char]): MailSender =
    new DefaultMailSender(sessionFactory(host, port, user, password))
  
  def senderKey(host: String, port: Int = -1, user: String, password: Array[Char]) = Key(host+port+user, {
    case Context => sender(host, port, user, password)
  })

}
