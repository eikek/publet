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

package org.eknet.publet.web.req

import javax.servlet.http.HttpServletRequest
import org.eknet.publet.web.PubletRequestWrapper
import org.eknet.publet.web.asset.AssetManager
import org.eknet.publet.web.req.RequestHandlerFactory._
import org.eknet.publet.web.filter.Filters

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 30.09.12 10:26
 */
class AssetsHandlerFactory extends RequestHandlerFactory with PubletRequestWrapper {

  def getApplicableScore(req: HttpServletRequest) =
    if (req.applicationUri.startsWith(AssetManager.assetPath)) EXACT_MATCH else NO_MATCH

  def createFilter() = new SuperFilter(Seq(
    Filters.guice,
    Filters.webContext,
    Filters.assets,
    Filters.source
  ))
}
