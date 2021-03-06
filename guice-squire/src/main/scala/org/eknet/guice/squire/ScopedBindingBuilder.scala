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

package org.eknet.guice.squire

import com.google.inject.binder.{ScopedBindingBuilder => GScopedBindingBuilder}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 08.11.12 14:56
 */
class ScopedBindingBuilder(val self: GScopedBindingBuilder) extends Proxy {

  import java.lang.annotation.{Annotation => JAnnotation}
  import SquireBinder._

  def as[A <: JAnnotation: Manifest]() {
    self.in(classFor[A])
  }

}

object ScopedBindingBuilder {

  implicit def enrichScopeBuilder(sb: GScopedBindingBuilder) = new ScopedBindingBuilder(sb)
  implicit def toScopeBuilder(eb: ScopedBindingBuilder) = eb.self

}