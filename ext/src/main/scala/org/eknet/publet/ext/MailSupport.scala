/*
 * Copyright 2012 Eike Kettner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.eknet.publet.ext

import javax.mail.internet.InternetAddress
import org.eknet.squaremail._
import org.eknet.publet.web.util.PubletWeb

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 14.04.12 17:49
 */
trait MailSupport {

  import org.eknet.squaremail.Messages._

  implicit def email(str: String) = parseAddress(str)

  implicit def str2CharArray(str: String): Array[Char] = str.toCharArray

  implicit def mail2EasyMail(m: MailMessage) = new EasyMail(m)

  def newMail(from: InternetAddress) = new DefaulMailMessage(from)

  class EasyMail(mail: MailMessage) {
    def send() {
      PubletWeb.instance[MailSender].get.send(mail)
    }

    def to(em: InternetAddress) = {
      mail.addTo(em)
      this
    }

    def subject(s: String) = {
      mail.setSubject(s)
      this
    }

    def text(t: String) = {
      mail.setText(t)
      this
    }

    def html(t: String) = {
      mail.setHtmlText(t)
      this
    }
  }

}
