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

package org.eknet.publet.ext.jmx

import javax.management.remote.{JMXPrincipal, JMXAuthenticator}
import com.google.inject.Singleton
import grizzled.slf4j.Logging
import javax.security.auth.Subject
import java.util.Collections
import org.apache.shiro.{ShiroException, SecurityUtils}
import org.apache.shiro.authc.{AuthenticationException, UsernamePasswordToken}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 24.11.12 15:52
 */
class JmxAuthenticator extends JMXAuthenticator with Logging {

  def authenticate(credentials: Any) = {
    if (credentials == null) {
      throwFailure("Credentials required!")
    }

    credentials match {
      case c: Array[AnyRef] if (c.length == 2) => {
        val username = Option(c(0)).map(_.toString).getOrElse(throwFailure("No username given"))
        val password = Option(c(1)).map(_.toString).getOrElse(throwFailure("No password given"))
        try {
          SecurityUtils.getSubject.login(new UsernamePasswordToken(username, password))
          SecurityUtils.getSubject.checkPermission("jmx:connector")
          createSubject(username)
        }
        catch {
          case e:ShiroException => throwFailure(e.getMessage)
        }
        null
      }
      case _ => throwFailure("Invalid credentials.")
    }
  }

  private[this] def createSubject(username: String) = {
    new Subject(true, Collections.singleton(new JMXPrincipal(username)), Collections.EMPTY_SET, Collections.EMPTY_SET)
  }

  private[this] def throwFailure(msg: String): Nothing = {
    val exc = new SecurityException("JMX Authentication failed! "+ msg)
    error(exc)
    throw exc
  }
}
