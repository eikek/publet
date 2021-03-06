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

import org.fusesource.scalate.TemplateEngine
import org.eknet.publet.Publet
import org.eknet.publet.engine.scalate.{ScalateEngineImpl, VfsResourceLoader}
import scalate.Boot
import org.eknet.publet.web.Config
import org.eknet.publet.web.asset.AssetManager

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 20.05.12 23:47
 */
class ConfiguredScalateEngine(name: Symbol, publet: Publet, config: Config, assetMgr: AssetManager)
  extends ScalateEngineImpl(name, ConfiguredScalateEngine.createEngine(config, publet, assetMgr)) {

  VfsResourceLoader.install(engine, publet)

  override def setDefaultLayoutUri(uri: String) {
    engine.layoutStrategy = new LayoutLookupStrategy(engine, new IncludeLoader(config, publet, assetMgr), uri)
  }

}

object ConfiguredScalateEngine {

  private def createEngine(config: Config, publet: Publet, assetMgr: AssetManager) = {
    val engine = new TemplateEngine()
    engine.workingDirectory = config.newStaticTempDir("scalate")
    engine.allowCaching = true
    engine.allowReload = true

    val loader = new IncludeResourceLoader(engine.resourceLoader, config, publet, assetMgr)
    engine.resourceLoader = loader

    new Boot(engine).run()
    engine
  }
}