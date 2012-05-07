package org.eknet.publet.web.scripts

import org.eknet.publet.engine.scala.ScalaScript
import ScalaScript._
import org.eknet.publet.web.shiro.Security
import org.apache.shiro.authz.UnauthenticatedException
import org.eknet.publet.web.{WebPublet, WebContext}
import org.eknet.publet.gitr.RepositoryName
import org.eknet.publet.vfs.Path
import org.eknet.publet.web.template.Javascript

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 07.05.12 22:45
 */
object GitrNewRepository extends ScalaScript with Javascript {

  def formMarkup = makeHtml {
    <h2>Create a new Repository</h2>
    <p>Create a new git repository. Use a short repository name that may not contain dashes and other weird characters.</p>
    <div id="formResponse"></div>
    <form id="newRepositoryForm" action={WebContext().requestPath.asString} method="post" class="ym-form ym-full linearize-form" >
      <div class="ym-fbox-text">
        <label for="repositoryName">Repository name</label>
          <input type="text" name="repositoryName" id="from" size="20" />
      </div>
      <div class="ym-fbox-text">
        <label for="description">Description (optional)</label>
          <input type="text" name="description" id="from" size="20" />
      </div>
      <input type="hidden" name="a" value="include"/>
      <button class="ym-button ym-add" onClick="return formAjaxSubmit('newRepositoryForm', 'formResponse');">Create repository</button>
    </form>
  }

  def stripDotGitExtension(name: String) = if (name.endsWith(".git")) name.substring(0, name.length-4) else name

  def createRepository(name: String, descr: Option[String]) = {
    if (!name.matches("[\\w]+")) {
      jsFunction(message("Repository name is invalid!", Some("error")))
    } else {
      val username = Security.user.map(_.login).getOrElse(throw new UnauthenticatedException())
      val reponame = username +"/"+ stripDotGitExtension(name)
      Security.checkPerm(Security.gitCreate, Path(reponame))
      val gitr = WebPublet().gitr
      val rname = RepositoryName(reponame+".git")
      if (gitr.exists(rname)) {
        jsFunction(message("Repository already exists!", Some("error")))
      } else {
        //todo permissions??
        gitr.create(rname, true)
        gitr.setExportOk(rname, true)
        jsFunction("window.location='"+ WebContext().requestPath.sibling("myrepos.html").asString +"'")
      }
    }
  }

  def serve() = {
    Security.checkAuthenticated()
    Security.checkPerm(Security.gitCreate, Path(Security.username))
    WebContext().parameter("repositoryName") match {
      case Some(n)=> createRepository(n, WebContext().parameter("description"))
      case None => formMarkup
    }
  }
}
