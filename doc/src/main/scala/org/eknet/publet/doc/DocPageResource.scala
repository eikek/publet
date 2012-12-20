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

import org.eknet.publet.vfs.{ContentType, ResourceName, ContentResource}
import java.io.ByteArrayInputStream

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 19.12.12 23:01
 */
class DocPageResource(p: DocPage) extends ContentResource {

  val name = p.resource.name.withExtension(ContentType.page.extensions.head)
  val exists = true
  val contentType = ContentType.page

  def inputStream = new ByteArrayInputStream(wrappedTempl.getBytes)

  override def lastModification = p.resource.lastModification

  override def length = Some(wrappedTempl.length.toLong)

  val wrappedTempl =
    """---
      |title: ${menuName} - Publet Documentation
      |
      |--- name:navigationBar pipeline:jade
      |=include("incl/_includes/nav.jade")
      |
      |--- name:content pipeline:jade
      |=include("incl/_includes/header.jade")
      |.row
      |  .span3
      |    =include("_sidebar.ssp")
      |  .span9
      |    =include("${resourceName}")
    """.stripMargin
      .replace("${resourceName}", p.resource.name.invisibleName.fullName)
      .replace("${menuName}", p.menuName)
}
