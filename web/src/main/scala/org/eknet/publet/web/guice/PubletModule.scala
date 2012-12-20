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

package org.eknet.publet.web.guice

import com.google.inject.{AbstractModule, Module}
import java.net.URL
import org.eknet.publet.web.util.AppSignature
import org.eknet.guice.squire.SquireModule

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 20.12.12 17:29
 */
trait PubletModule extends Module {

  def name: String
  def version: String
  def license: Option[(String, URL)]
  def homePage: Option[URL]
}

/**
 * Abstract guice module that mixes in [[org.eknet.publet.web.guice.PubletModule]]
 * and implements `version` and `license` returning the values of the publet app.
 */
abstract class AbstractPubletModule extends SquireModule with PubletModule {
  val version = AppSignature.version
  val license = AppSignature.license
  val homePage: Option[URL] = None
}
