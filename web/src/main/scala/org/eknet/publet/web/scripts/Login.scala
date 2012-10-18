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

package org.eknet.publet.web.scripts

import org.eknet.publet.web.shiro.Security
import org.apache.shiro.authc.UsernamePasswordToken
import org.eknet.publet.engine.scala.ScalaScript
import ScalaScript._
import org.apache.shiro.ShiroException
import org.eknet.publet.web.util.PubletWebContext

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 03.05.12 20:30
 */
object Login extends ScalaScript {

  def serve() = {
    val username = PubletWebContext.param("username")
    val password = PubletWebContext.param("password")
    val rememberMe = PubletWebContext.param("rememberMe")
    if (username.isDefined && password.isDefined) {
      val subject = Security.subject
      val token = new UsernamePasswordToken(username.get, password.get.toCharArray)
      token.setRememberMe(checkboxToBoolean(rememberMe))
      try {
        subject.login(token)
        makeJson(Map("success"->true, "message"->"Login successful."))
      } catch {
        case e:ShiroException => {
          makeJson(Map("success"->false, "message"->"Login failed."))
        }
      }
    } else {
      makeJson(Map("success"->false, "message"->"No login information given."))
    }
  }

  def checkboxToBoolean(str: Option[String]): Boolean = {
    str.exists(_ == "on")
  }
}
