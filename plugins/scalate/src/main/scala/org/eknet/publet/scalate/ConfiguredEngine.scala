package org.eknet.publet.scalate

import org.fusesource.scalate.TemplateEngine

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 21.05.13 23:20
 */
class ConfiguredEngine(val engine: TemplateEngine, var attributes: Map[String, Any] = Map.empty)
