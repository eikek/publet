package org.eknet.publet.webeditor

import xml.{Null, Text, Attribute}
import org.eknet.publet.web.{PubletWeb, PubletWebContext}
import org.eknet.publet.vfs.{Path, ContentType}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 20.05.12 01:40
 */
class EditHelper {

  def extensionOptions(path: Path) = {
    val publet = PubletWeb.publet
    val source = publet.findSources(path).headOption
    val list = ContentType.forMimeBase(PubletWebContext.applicationPath.name.targetType)
    val prefExt = source.map(_.name.ext).getOrElse("md")
    for (ct <- list) yield {
      <optgroup label={ct.typeName.name}>
      {
        for (ext<-ct.extensions.toList.sortWith(_ < _)) yield {
          val o = <option>{ext}</option>
          if (prefExt == ext) {
            o % Attribute("selected", Text("selected"), Null)
          } else {
            o
          }
        }
      }
      </optgroup>
    }
  }
}
