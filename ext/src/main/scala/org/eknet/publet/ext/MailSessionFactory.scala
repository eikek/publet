/*
 * Copyright 2013 Eike Kettner
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

import org.eknet.squaremail.DefaultSessionFactory
import com.google.inject.{Singleton, Inject}
import org.eknet.publet.web.Config

/**
 * Factory for mail [[javax.mail.Session]]s. This is used from within
 * the bound [[org.eknet.squaremail.MailSender]].
 *
 * The given config object is used to setup all properties (smtp host, port,
 * username, password, smtp auth, socketFactoryClass, startTLS, useSSL).
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 10.02.13 21:59
 */
@Singleton
class MailSessionFactory @Inject()(config: Config) extends DefaultSessionFactory with MailSessionFactoryMBean {

  setSmtpHost(config("smtp.host").getOrElse(""))
  setSmtpPort(config("smtp.port").getOrElse("-1").toInt)
  setSmtpUsername(config("smtp.username").getOrElse(""))
  setSmtpPassword(config("smtp.password").getOrElse("").toCharArray)
  setSmtpAuth(config("smtp.auth").getOrElse("false").toBoolean)
  setSocketFactoryClass(config("smtp.socketFactoryClass").orNull)
  setStartTLS(config("smtp.startTLS").getOrElse("false").toBoolean)
  setUseSsl(config("smtp.useSSL").getOrElse("false").toBoolean)

}
