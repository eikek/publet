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

package org.eknet.publet.engine.scalate

import org.fusesource.scalate.TemplateEngine
import org.fusesource.scalate.util.ResourceLoader
import org.fusesource.scalate.layout.DefaultLayoutStrategy
import org.fusesource.scalate.support.URLTemplateSource
import org.eknet.publet.vfs.{Path, ContentType, Content}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 19.05.12 00:14
 */
object TestSome {

  def loadDefaultLayout = Option(classOf[ScalateEngine].getResource("default.jade")).get

  def main(args: Array[String]) {
    val engine = new TemplateEngine

    val mytempl = "<%@ var name:(String,String) %>\n" +
      "<%@ var city:String %>\n" +
      "<% attributes(\"title\") = \"This is my new title\" %>\n" +
      "<p> Hello ${name._1} ${name._2}, from ${city}. </p>"
    val mdtempl = "# headline\n\n"+
      "thanks for the text."
    val htmltempl = "<h1>Hello</h1><p>this is plain html.</p>"

    val templ = new TemplateResource(Path("mytempl.ssp"), Content(htmltempl, ContentType.ssp))

    engine.resourceLoader = new ResourceLoader {
      def resource(uri: String) = Some(if (uri == "default.jade") new URLTemplateSource(loadDefaultLayout) else templ)
    }
    engine.layoutStrategy = new DefaultLayoutStrategy(engine, "default.jade")

    for (i <- 0 to 5) {
      val start = System.currentTimeMillis()
      println(engine.layout(templ,  Map("name" -> ("Hiram", "Chirino"), "city" -> "Tampa", "title" -> "My Title")))
      println(">>> " + (System.currentTimeMillis() - start))
    }

    System.exit(0)
  }
}
