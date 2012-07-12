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

package org.eknet.publet.impl

import scala.collection.mutable
import org.eknet.publet._
import engine.{PubletEngine, EngineMangager}
import vfs._
import java.io.InputStream
import grizzled.slf4j.Logging

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 28.03.12 22:43
 */
class PubletImpl extends MountManager with Publet with EngineMangager with RootContainer with Logging {

  def process(path: Path): Option[Content] = {
    Predef.ensuring(path != null, "null is illegal")
    process(path, path.name.targetType)
  }

  def process(path: Path, targetType: ContentType, engine: Option[PubletEngine] = None): Option[Content] = {
    // lookup the source
    findSources(path) match {
      case Nil => None
      //lookup the engine according to the uri scheme and process data
      case data => {
        if (data.size > 1) warn("More than one source file found for: "+ path.asString)
        val sourcePath = path.sibling(data.head.name.fullName)
        val eng = engine.orElse(resolveEngine(sourcePath))
          .getOrElse(sys.error("No engine found for uri: "+ path.asString))

        eng.process(path, data.head, targetType)
      }
    }
  }

  def push(path: Path, content: InputStream, changeInfo: Option[ChangeInfo] = None) = {
    def copy(ext: Option[String]) = {
      createResource(path, ext) match {
        case cr: Writeable => cr.writeFrom(content, changeInfo); cr.asInstanceOf[ContentResource]
        case r@_ => sys.error("Cannot create content for resource: "+ r)
      }
    }
    findSources(path).toList match {
      case Nil => copy(None)
      case c::cs => {
        if (c.name.ext == path.name.ext) {
          copy(Some(c.name.ext))
        } else {
          Resource.toModifyable(c).map(_.delete()).getOrElse(sys.error("Resource not modifyable"))
          copy(Some(path.name.ext))
        }
      }
    }
  }


  def delete(path: Path) {
    findSources(path).toList map { c =>
      Resource.toModifyable(c).map(_.delete()).getOrElse(sys.error("Resource not modifyable"))
    }
  }

  def findSources(path: Path): Iterable[ContentResource] = {
    Predef.ensuring(path != null, "null is illegal")
    if (path.directory) Seq()
    else {
      // at first try by listing all children in the container
      // if that results in an empty list, try looking up files
      // with the same name and all known extensions. The latter
      // case implies case sensitivtiy of the underlying platform
      // and is a fallback for containers that don't support listing
      // its children (like ClasspathContainer). Only lowercase
      // extensions are used for lookups!

      lookup(path.parent).collect({case c: ContainerResource=>c})
        .map(_.children.filter(_.name.name == path.name.name))
        .filter(!_.isEmpty)
        .map(_.collect({case c: ContentResource=> c}))
        .getOrElse {

          // all extensions but the one requested
          val allexts = ContentType.all.filter(_ != path.name.targetType).flatMap(_.extensions)

          // requested extensions
          val reqexts = path.name.targetType.extensions

          //lookup first sources with requested extensions
          val rsources = reqexts.map(ext => lookup(path.withExt(ext))).collect({case Some(cc:ContentResource) => cc}).toSeq
          val others = mutable.ListBuffer[ContentResource]()
          if (rsources.isEmpty) {
            others ++= allexts.map(ext => lookup(path.withExt(ext))).collect({case Some(cc:ContentResource) => cc})
          }
          rsources ++ others.toSeq
      }
    }
  }

  def createResource(path: Path, ext: Option[String]): Resource = {
    val p = if (ext.isDefined) path.withExt(ext.get) else path
    createResource(p)
  }

  def mountManager = this

  def engineManager = this

  def rootContainer = this

}


