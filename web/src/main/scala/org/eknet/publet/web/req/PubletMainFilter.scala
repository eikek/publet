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

import javax.servlet._
import http.{HttpServletResponse, HttpServletRequest}
import RequestHandlerFactory._
import grizzled.slf4j.Logging

/**
 * Publet's main filter.
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 27.09.12 15:42
 */
class PubletMainFilter extends Filter with Logging {

  val handlerFactories = List(
    new GitHandlerFactory,
    new WebdavHandlerFactory,
    new PubletHandlerFactory
  )

  private var handlerMap: Map[Class[_], Filter] = null

  def init(filterConfig: FilterConfig) {
    handlerMap = (for (factory <- handlerFactories) yield {
      factory.getClass -> factory.createFilter()
    }).toMap
    handlerMap.values.foreach(_.init(filterConfig))
  }

  def doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
    val start = if (isDebugEnabled) Some(System.currentTimeMillis()) else None
    val req = request.asInstanceOf[HttpServletRequest]
    val handler = handlerFactories.foldLeft(nullHandler())((f1, f2) => {
      f1.getApplicableScore(req) match {
        case score1 if (score1 == NO_MATCH) => f2
        case score1 => {
          val score2 = f2.getApplicableScore(req)
          if (score1 > score2) f1 else f2
        }
      }
    })
    try {
      handlerMap.get(handler.getClass).get.doFilter(request, response, chain)
    } catch {
      case e: Throwable => {
        //this is last resort. all exceptions should be properly handled in filters above
        val res = response.asInstanceOf[HttpServletResponse]
        error("Error during request: "+ req.getRequestURI, e)
        res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
      }
    }
    finally {
      start.foreach(s => debug("--- Request '"+req.getRequestURI+"': " + (System.currentTimeMillis()-s)+"ms"))
    }
  }

  def destroy() {
    handlerMap.values.foreach(_.destroy())
  }
}
