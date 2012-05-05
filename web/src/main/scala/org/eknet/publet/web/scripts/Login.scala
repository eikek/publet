package org.eknet.publet.web.scripts

import org.eknet.publet.web.WebContext
import org.eknet.publet.vfs.Path
import org.eknet.publet.web.shiro.Security
import org.apache.shiro.authc.UsernamePasswordToken
import org.eknet.publet.engine.scala.ScalaScript
import ScalaScript._
import org.apache.shiro.ShiroException
import xml.NodeSeq
import org.eknet.publet.web.template.Javascript

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 03.05.12 20:30
 */
object Login extends ScalaScript with Javascript {

  def actionUrl(page:String) = WebContext.rebasePath(Path("/publet/scripts/"+page+".html"))

  def loginForm = {
    val redirectParam = WebContext().parameter("redirect")
    val node = redirectParam.map(uri => <input type="hidden" name="redirect" value={uri}/>).getOrElse(NodeSeq.Empty)

    makeHtml {
      <h1>Login</h1>
        <div id="loginResponse"></div>
        <form id="loginForm" class="ym-form ym-full linearize-form" style="width:400px; margin:auto;" action={ actionUrl("login").asString } method="post">
          { node }
          <div class="ym-fbox-text">
            <label for="username">Username
              <sup class="ym-required">*</sup>
            </label>
              <input type="text" name="username" id="from" size="20" required="required"/>
          </div>
          <div class="ym-fbox-text">
            <label for="password">Password
              <sup class="ym-required">*</sup>
            </label>
              <input type="password" name="password" rows="10" required="required"/>
          </div>
          <input type="hidden" name="a" value="include"/>
          <button class="ym-button ym-next" onClick="return formAjaxSubmit('loginForm', 'loginResponse');">Login</button>
        </form>
    }
  }

  def alreadyLoggedIn = makeHtml {
    <span class="logoutSnippet">Welcome, { Security.username }! <a class="ym-button ym-next" href={ actionUrl("logout").asString }>Logout</a></span>
  }

  def loginLink = makeHtml {
    <a class="ym-button ym-next" href={ actionUrl("login").asString }>Login</a>
  }

  def markup() = {
    if (Security.isAuthenticated) {
      alreadyLoggedIn
    } else {
      if (WebContext().parameter("loginLink").isDefined) {
        loginLink
      } else {
        loginForm
      }
    }
  }


  def serve() = {
    val ctx = WebContext()

    val username = ctx.parameter("username")
    val password = ctx.parameter("password")
    val redirectUri = ctx.parameter("redirect")
    if (username.isDefined && password.isDefined) {
      val subject = Security.subject
      val token = new UsernamePasswordToken(username.get, password.get.toCharArray)
      token.setRememberMe(true)
      try {
        subject.login(token)
        jsFunction("window.location='"+redirectUri.getOrElse("/")+"'")
      } catch {
        case e:ShiroException => jsFunction(message("Login failed!", Some("error")))
      }
    } else {
      markup()
    }
  }
}
