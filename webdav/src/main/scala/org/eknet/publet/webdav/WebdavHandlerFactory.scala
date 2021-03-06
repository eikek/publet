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

package org.eknet.publet.webdav

import javax.servlet.http.HttpServletRequest
import org.eknet.publet.web.filter._
import org.eknet.publet.web.{WebExtension, ReqUtils}
import com.google.inject.{Inject, Singleton}
import org.eknet.publet.web.req.{SuperFilter, RequestHandlerFactory}
import RequestHandlerFactory._
import org.eknet.publet.Publet
import org.eknet.publet.web.guice.PubletStartedEvent
import com.google.common.eventbus.Subscribe
import org.eknet.publet.vfs.util.ClasspathContainer
import org.eknet.publet.vfs.Path

/**
 * Creates a filter chain to handle webdav requests.
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 27.09.12 15:35
 */
@Singleton
class WebdavHandlerFactory @Inject() (webext: java.util.Set[WebExtension], publet: Publet) extends RequestHandlerFactory {

  def getApplicableScore(req: HttpServletRequest) = {
    val requtil = new ReqUtils(req) with WebdavRequestUtil
    if (requtil.isDavRequest) EXACT_MATCH else NO_MATCH
  }

  def createFilter() = new SuperFilter(Seq(
    Filters.webContext,
    Filters.authc,
    Filters.exceptionHandler,
    Filters.extensionRequest(webext),
    new WebdavFilter(publet)
  ))

  @Subscribe
  def mountDirectoryListingTemplate(e: PubletStartedEvent) {
    val c = new ClasspathContainer(base = "/org/eknet/publet/webdav/templates")
    publet.mountManager.mount(Path("/publet/webdav/listing/"), c)
  }
}
