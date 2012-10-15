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

import com.google.inject.name.{Named, Names => GuiceNames}
import com.google.inject
import inject.Injector

/**
 * Helper methods when dealing with [[com.google.inject.Injector]]. Mainly
 * to avoid typing `classOf` since Scala carries type information that can
 * be used.
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 07.10.12 02:40
 */
trait InjectorHelper {

  def injector: Injector

  /**
   * Uses the Guice Injector to retrieve an instance of the given
   * type. This just works for lookups of exactly one instance.
   *
   * @tparam T
   * @return
   */
  def instance[T:Manifest]:T = {
    val c = manifest[T].erasure
    injector.getInstance(c).asInstanceOf[T]
  }

  /**
   * Uses the Guice Injector to retrieve an instsance of the
   * given type named with the given annotation.
   *
   * @param name
   * @tparam T
   * @return
   */
  def instance[T:Manifest](name: Named): T = {
    val c = manifest[T].erasure
    injector.getInstance(inject.Key.get(c, name)).asInstanceOf[T]
  }

  /**
   * Uses the Guice Injector to retrieve an instance of the
   * given type named with the given string.
   * @param n
   * @tparam T
   * @return
   */
  def instance[T:Manifest](n: String): T = instance(GuiceNames.named(n))

}
