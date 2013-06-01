package org.eknet.publet.webapp

import org.eclipse.jgit.http.server.GitServlet
import org.eknet.publet.webapp.servlet._
import spray.routing.authentication.BasicAuth
import spray.http._
import spray.routing.{RequestContext, Directive0}
import scala.concurrent.Future
import org.eknet.publet.gitr.{RepoName, GitrExtension}
import spray.http.HttpResponse
import spray.routing.authentication.UserPass
import spray.http.HttpChallenge
import scala.Some
import org.eknet.publet.content.Path
import org.eknet.publet.gitr.RepositoryManager.SyncTandem

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 26.05.13 00:51
 */
trait GitServletExtension extends WebExtension {
  self: ServletExtension =>

  private val gitPath = "git"
  private lazy val gitServlet = (new GitServlet)
    .withConfig("base-path" -> GitrExtension(system).root.toAbsolutePath.toString, "export-all" -> "true")
    .withServletPath(gitPath)

  withRoute {
    pathPrefix(gitPath) {
      extract(repoName) { name =>
        authenticate(BasicAuth("Git Repos")) { user =>
          detachTo(singleRequestServiceActor) {
            publishCommitEvent(name) {
              complete(gitServlet.withRemoteUser(user.username))
            }
          }
        } ~
        complete(unauthorizedChallenge())
      }
    }
  }

  private def publishCommitEvent(name: RepoName): Directive0 = mapRequestContext { ctx =>
    ctx.mapRouteResponse { in =>

      if (ctx.request.path.endsWith("receive-pack")) {
        system.eventStream.publish(GitrPushEvent(name))
        GitrExtension(system).repoMan ! SyncTandem(name)
      }
      else if (ctx.request.path.endsWith("upload-pack")) {
        system.eventStream.publish(GitrPullEvent(name))
      }

      in
    }
  }

  def repoName(ctx: RequestContext) = {
    val name = Path(ctx.request.path).drop(Path(gitPath).segments.length)
    RepoName(name.take(name.size-1))
  }

  private def unauthorizedChallenge() = {
    val challenge = HttpChallenge("basic", "Git Repos")
    HttpResponse(status = StatusCodes.Unauthorized, entity = EmptyEntity, headers = List(HttpHeaders.`WWW-Authenticate`(challenge)))
  }

  def authenticateUser(userpass: Option[UserPass]): Future[Option[String]] = userpass match {
    case Some(up) => Future.successful(Some(up.user))
    case _ => Future.successful(None)
  }
}

case class GitrPushEvent(repository: RepoName)
case class GitrPullEvent(repository: RepoName)