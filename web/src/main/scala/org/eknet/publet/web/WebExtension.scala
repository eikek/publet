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

package org.eknet.publet.web

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 26.04.12 20:31
 */
trait WebExtension {

  /**
   * Point on which extension code is executed
   * once per server start.
   *
   * It is ensured, that those are invoked
   * _after_ [[org.eknet.publet.web.PubletWeb]]
   * has been initialized.
   */
  def onStartup()

  /**
   * This method is invoked when the servlet container is
   * shutting down.
   *
   */
  def onShutdown()

  /**
   * This method is invoked on the begin of each request.
   *
   */
  def onBeginRequest()

  /**
   * This method is invoked on the end of each request.
   *
   */
  def onEndRequest()

}

trait EmptyExtension extends WebExtension {

  def onStartup() {}
  def onShutdown() {}
  def onBeginRequest() {}
  def onEndRequest() {}

}
