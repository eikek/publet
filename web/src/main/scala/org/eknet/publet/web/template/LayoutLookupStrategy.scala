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

package org.eknet.publet.web.template

import org.fusesource.scalate.{RenderContext, Template, TemplateEngine}
import org.fusesource.scalate.layout.{DefaultLayoutStrategy, LayoutStrategy}
import org.eknet.publet.web.{PubletWebContext, IncludeLoader}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 20.05.12 23:19
 */
class LayoutLookupStrategy(val engine: TemplateEngine, defaultLayouts: String*) extends LayoutStrategy {

  private val delegate = new DefaultLayoutStrategy(engine, defaultLayouts: _*)
  private val loader = new IncludeLoader()

  val layoutName = "pageLayout"
  val layoutCandidates = engine.extensions.map(layoutName +"."+ _)

  def layout(template: Template, context: RenderContext) {
    PubletWebContext.param("noLayout") match {
      case Some(_) => context.attributes.update("layout", "")
      case _ =>
    }
    context.attributes.get("layout") getOrElse {
      findLayout(layoutCandidates.toList) foreach { layout =>
        context.attributes.update("layout", layout)
      }
    }
    delegate.layout(template, context)
  }

  private def findLayout(cand: List[String]): Option[String] = {
    cand match {
      case c::cs => loader.findInclude(c) orElse {
        findLayout(cs)
      }
      case Nil => None
    }
  }
}
