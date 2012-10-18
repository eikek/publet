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

package org.eknet.publet.webeditor.actions

import org.eknet.publet.engine.scala.ScalaScript
import ScalaScript._
import grizzled.slf4j.Logging
import org.eknet.publet.vfs.{ContentResource, Path}
import collection.mutable
import org.eknet.publet.webeditor.EditorPaths
import org.eknet.publet.web.shiro.Security
import org.eknet.publet.web._
import util.{RenderUtils, PubletWeb, PubletWebContext}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 23.05.12 19:17
 */
object FileUploadHandler extends ScalaScript with Logging with ThumbnailUrlUtil {

  lazy val pushJson = EditorPaths.pushScript.asString
  lazy val uploadJson = EditorPaths.uploadScript.asString


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
        val commitMsg = PubletWebContext.param("commitMessage").getOrElse("")
        val out = files.map( fi => {
          val path = container.get / fi.getName
          Security.checkWritePermission(path)
          info("Uploading to "+ path.asString)
          (path, PubletWeb.publet.push(path, fi.getInputStream, Security.changeInfo(commitMsg)))
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
    val ctx = PubletWebContext
    val map = mutable.Map[String, Any]()
    map.put("name", res.name.fullName)
    map.put("size", res.length.getOrElse(0L))
    map.put("url", ctx.urlOf(path.sibling(res.name.fullName).asString))
    map.put("thumbnail_url", thumbnailUrl(path.sibling(res.name.fullName), res))
    map.put("delete_url", ctx.urlOf(pushJson+"?delete=" + path.sibling(res.name.fullName).asString))
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
