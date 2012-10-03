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

package org.eknet.publet.vfs

import java.io.File
import org.eknet.publet.impl.Conversions._
import eu.medsea.mimeutil.{MimeType, MimeUtil2, MimeUtil}
import eu.medsea.mimeutil.detector.{MagicMimeMimeDetector, ExtensionMimeDetector}

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 29.03.12 10:11
 */
case class ContentType(typeName: Symbol, extensions: Set[String], mime: (String, String)) {

  val mimeString = mime._1 +"/"+ mime._2
}

object ContentType {
  MimeUtil.registerMimeDetector(classOf[ExtensionMimeDetector].getName)
  MimeUtil.registerMimeDetector(classOf[MagicMimeMimeDetector].getName)

  val scal = ContentType('scala, Set("scala"), ("text", "scala"))
  val text = ContentType('text, Set("txt", "text", "cfg", "properties"), ("text", "plain"))
  val html = ContentType('html, Set("html", "htm"), ("text", "html"))
  val markdown = ContentType('markdown, Set("md", "markdown"), ("text", "markdown"))
  val textile = ContentType('textile, Set("textile"), ("text", "textile"))
  val confluence = ContentType('confluence, Set("conf"), ("text", "conf"))
  val page = ContentType('page, Set("page"), ("text", "page"))
  val feed = ContentType('feed, Set("feed"), ("text", "feed"))
  val ssp = ContentType('ssp, Set("ssp"), ("text", "ssp"))
  val scaml = ContentType('scaml, Set("scaml"), ("text", "scaml"))
  val mustache = ContentType('mustache, Set("mustache"), ("text", "mustache"))
  val jade = ContentType('jade, Set("jade"), ("text", "jade"))
  val xml = ContentType('xml, Set("xml"), ("text", "xml"))
  val css = ContentType('css, Set("css"), ("text", "css"))
  val javascript = ContentType('javascript, Set("js"), ("text", "javascript"))
  val json = ContentType('json, Set("json"), ("application", "json"))

  val png = ContentType('png, Set("png"), ("image", "png"))
  val jpg = ContentType('jpg, Set("jpg", "jpeg"), ("image", "jpg"))
  val gif = ContentType('gif, Set("gif"), ("image", "gif"))
  val icon = ContentType('icon, Set("ico"), ("image", "x-icon"))

  val pdf = ContentType('pdf, Set("pdf"), ("application", "pdf"))
  val zip = ContentType('zip, Set("zip", "gz", "bz2"), ("application", "zip"))
  val jar = ContentType('jar, Set("jar"), ("application", "java-archive"))
  val unknown = ContentType('unknown, Set(), ("application", "octet-stream"))

  val all = Set(text, html, markdown, textile, confluence, page, feed, ssp, scaml,
    mustache, jade, xml, css, javascript, json, png, jpg, gif, icon, scal, pdf, jar, zip, unknown)

  def apply(f: File): ContentType = apply(extension(f))

  private def extension(f: File): String = Path(f).name.ext

  def apply(ext: String): ContentType = {
    all.find(_.extensions.contains(ext.toLowerCase))
      .getOrElse {
      if (markdownExtensions.contains(ext)) ContentType.markdown
      else unknown
    }
  }

  def apply(name: Symbol): ContentType = {
    all.find(_.typeName == name)
      .orElse(throwException("Unknown type: " + name)).get
  }

  def apply(mime: (String, String)): ContentType = {
    all.find(_.mime == mime)
      .orElse(throwException("Unknown mime type: " + mime)).get
  }

  def forMimeBase(t: ContentType): Seq[ContentType] = all.toSeq.filter(_.mime._1 == t.mime._1)

  def getMimeType(filename: String): String = {
    val t = ContentType(MimeUtil.getExtension(filename))
    if (t == ContentType.unknown) {
      import collection.JavaConversions._
      MimeUtil.getMimeTypes(filename).headOption.getOrElse(MimeUtil2.UNKNOWN_MIME_TYPE).toString
    } else {
      t.mimeString
    }
  }

  def isTextType(filename: String): Boolean = {
    val t = ContentType(MimeUtil.getExtension(filename))
    if (t == ContentType.unknown) {
      import collection.JavaConversions._
      val types = MimeUtil.getMimeTypes(filename).collect({case m:MimeType=>m})
      types.exists(mt => MimeUtil2.isTextMimeType(mt))
    } else {
      t.mime._1 == "text"
    }
  }

  def fromString(mime: String) = {
    val mimeRegex = "([^/]+)/(.*)".r
    mime match {
      case mimeRegex(b, s) => {
        all.find(_.mime ==(b, s)).getOrElse {
          val sym = Symbol(mime.replace('/', 'S'))
          ContentType(sym, Set(), (b, s))
        }
      }
      case _ => ContentType.unknown
    }
  }

  /** All those are also markdown files. Scalate only knows markdown and md; it throws
   * an exception for those extensions */
  val markdownExtensions = Set("mdown", "mkdn", "mkd", "mdwn", "mdtxt", "mdtext")
}
