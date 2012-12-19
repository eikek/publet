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

package org.eknet.publet.doc

import org.eknet.publet.vfs.{Content, ContentType, ResourceName, ContentResource}
import java.io.ByteArrayInputStream
import org.eknet.publet.web.util.{Request, Key, PubletWebContext}
import scala.xml.{Text, NodeSeq}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 19.12.12 22:44
 */
class SidebarResource(pages: List[_ <: MenuName]) extends ContentResource {
  val name = ResourceName("_sidebar.ssp")
  val exists = true
  val contentType = ContentType.ssp

  def inputStream = {
    PubletWebContext.attr(contentKey) match {
      case Some(c) => new ByteArrayInputStream(c.toString().getBytes)
      case _ => new ByteArrayInputStream(Array[Byte]())
    }
  }

  private[this] val contentKey = Key("sidebarContent", {
    case Request => createContent
  })

  private[this] def createContent: NodeSeq = {
    val current = PubletWebContext.applicationPath.name.name
    def createListItem(r: DocPage) = {
      val active = r.resource.name.name match {
        case `current` => Some(Text("active"))
        case _ => if (current == "index" && r == pages.head) {
          Some(Text("active"))
        } else {
          None
        }
      }
      <li class={active}>
        <a href={r.resource.name.withExtension("html").fullName}><i class={r.iconClass}></i> { r.menuName }</a>
      </li>
    }
    def createHeadline(m: MenuName) = {
      <li><h4>{m.menuName}</h4></li>
    }

    val x = pages map (p => p match {
      case p: DocPage => createListItem(p)
      case m@_ => createHeadline(m)
    })
    NodeSeq.seqToNodeSeq(<ul class="nav nav-pills nav-stacked">{x}</ul>)
  }
}
