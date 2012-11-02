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

import org.eknet.publet.engine.scala.ScalaScript
import org.eknet.publet.web.shiro.Security
import org.eknet.publet.auth.user.{User, UserProperty}
import org.eknet.publet.vfs.Content
import org.eknet.publet.web.util.{PubletWeb, PubletWebContext}
import org.eknet.publet.auth.Algorithm

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 19.07.12 16:21
 */
class MyDataScript extends ScalaScript {

  import ScalaScript._

  def serve(): Option[Content] = {
    if (!Security.isAuthenticated) {
      error("Not authenticated.")
    } else {
      PubletWebContext.param("what") match {
        case Some("setPassword") => {
          PubletWebContext.param("newPassword1").map (newPass1 => {
            PubletWebContext.param("newPassword2").flatMap (newPass2 => {
              val algorithm = PubletWebContext.param("algorithm").getOrElse("SHA-512")
              if (newPass1 == newPass2) changePassword(newPass1, algorithm)
              else error("Passwords do not match!")
            })
          }).getOrElse(error("Cannot change password!"))
        }
        case Some("setUserData") => {
          PubletWebContext.param("fullName").map(fullName => {
            PubletWebContext.param("email").flatMap(email => {
              changeUserData(fullName, email)
            })
          }).getOrElse(error("Cannot update user data!"))
        }
        case Some("getUserData") => getUserData
        case _ => error("No action specified.")
      }
    }
  }

  def success(msg: String) =  makeJson(Map("success"->true, "message"->msg))
  def error(msg: String) =  makeJson(Map("success"->false, "message"->msg))

  private def changePassword(newpassPlain: String, algorithm: String) = {
    PubletWeb.authManager.setPassword(Security.username, newpassPlain, Some(Algorithm.withName(algorithm)))
    success("Password updated.")
  }

  private def changeUserData(fullName: String, email: String) = {
    val user = PubletWeb.authManager.findUser(Security.username).get
    val props = user.properties +
      (UserProperty.fullName -> fullName) +
      (UserProperty.email -> email)
    val newUser = new User(user.login, props)
    PubletWeb.authManager.updateUser(newUser)
    success("User data updated.")
  }

  private def getUserData = {
    val user = PubletWeb.authManager.findUser(Security.username).get
    makeJson(Map(
      "success" -> true,
      "fullName" -> (user.get(UserProperty.fullName).getOrElse("")),
      "email" -> (user.get(UserProperty.email).getOrElse(""))
    ))
  }

}
