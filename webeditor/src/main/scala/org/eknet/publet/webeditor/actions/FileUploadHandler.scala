package org.eknet.publet.webeditor.actions

import org.eknet.publet.engine.scala.ScalaScript
import ScalaScript._
import grizzled.slf4j.Logging
import org.eknet.publet.vfs.{ContentResource, Path}
import collection.mutable
import org.eknet.publet.web.util.RenderUtils
import org.eknet.publet.webeditor.EditorPaths
import org.eknet.publet.web.shiro.Security
import org.eknet.publet.web._

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 23.05.12 19:17
 */
object FileUploadHandler extends ScalaScript with Logging {

  lazy val pushJson = PubletWebContext.urlBase + EditorPaths.pushScript.asString
  lazy val uploadJson = PubletWebContext.urlBase + EditorPaths.uploadScript.asString


  def serve() = {
    val publet = PubletWeb.publet
    PubletWebContext.getMethod match {
      case Method.get => {
        PubletWebContext.param("resource").map(Path(_))
          .flatMap(path => publet.rootContainer.lookup(path).map((path, _)))
          .collect({ case (p:Path, r: ContentResource) => (p, r)})
          .map(t => render(toMap(t._1, t._2)))
          .getOrElse(makeJson(Array()))
      }
      case Method.post => {
        val files = PubletWebContext.uploads
        val container = PubletWebContext.param("container").map(Path(_))
        val commitMsg = PubletWebContext.param("commitMessage").filter(!_.isEmpty)
        Security.checkGitAction(GitAction.push)
        val out = files.map( fi => {
          val path = container.get / fi.getName
          info("Uploading to "+ path.asString)
          (path, PubletWeb.publet.push(path, fi.getInputStream, commitMsg))
        }).map(t => toMap(t._1, t._2))
        render(out)
      }
      case Method.delete => {
        error("DELETE not implemented yet :(")
        RenderUtils.renderMessage("Not implemented", "Not implemented :(", "error")
      }
      case _=> {
        Some(ErrorResponse.methodNotAllowed)
      }
    }
  }

  def toMap(path: Path, res: ContentResource) = {
    val map = mutable.Map[String, Any]()
    map.put("name", res.name.fullName)
    map.put("size", res.length.getOrElse(0L))
    map.put("url", path.sibling(res.name.fullName).asString)
    map.put("thumbnail_url", EditorPaths.thumbNailer.asString+"?resource="+path.sibling(res.name.fullName).asString)
    map.put("delete_url", pushJson+"?delete=" + path.sibling(res.name.fullName).asString)
    map.put("delete_type", "DELETE")
    map.toMap
  }

  def render(value: Map[String, Any]) = {
    makeJson(Array(value))
  }

  def render(values: Iterable[Map[String, Any]]) = {
    makeJson(values)
  }
}
