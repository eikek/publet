package org.eknet.publet.web.scripts

import org.eknet.publet.web.shiro.Security
import org.apache.shiro.authc.UsernamePasswordToken
import org.eknet.publet.engine.scala.ScalaScript
import ScalaScript._
import org.apache.shiro.ShiroException
import org.eknet.publet.web.PubletWebContext

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
