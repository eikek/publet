package org.eknet.publet.web.scripts

import org.eknet.publet.engine.scala.ScalaScript
import ScalaScript._
import org.eknet.publet.web.shiro.Security
import org.apache.shiro.authz.UnauthenticatedException
import org.eclipse.jgit.lib.Repository
import org.eknet.publet.vfs.{ResourceName, Path}
import org.eknet.publet.web.PubletWeb
import org.eknet.publet.gitr.GitrRepository

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 07.05.12 22:59
 */
object GitrMyRepositories extends ScalaScript {
  def serve() = {
    val username = Security.user.map(_.login).getOrElse(throw new UnauthenticatedException())
    val gitr = PubletWeb.gitr
    makeRepositoryList(gitr.allRepositories {rn =>
      rn.name.startsWith(username+"/")
    })
  }

  def makeRepositoryList(repos:Iterable[GitrRepository]) = {
    makeHtml {
      <h2>My git repositories</h2 >
      <p class="box info">Your repositories</p>
      <ul>
      {
        for (r <- repos) yield {
          <li><a href="#">{ ResourceName(r.getDirectory.getName).name }</a></li>
        }
      }
      </ul>
    }
  }
}
