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

import com.google.inject.binder.{LinkedBindingBuilder => GLinkedBindingBuilder}
/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 08.11.12 14:55
 */
class LinkedBindingBuilder[A: Manifest](val self: GLinkedBindingBuilder[A]) {

  import SquireBinder._

  def to[B <: A: Manifest]: ScopedBindingBuilder =
    new ScopedBindingBuilder(self.to(classFor[B]))

}

object LinkedBindingBuilder {
  implicit def enrichLinkingBuilder[A: Manifest](lb: GLinkedBindingBuilder[A]) =
    new LinkedBindingBuilder[A](lb)

  implicit def toLinkingBuilder[A](eb: LinkedBindingBuilder[A]) = eb.self
}
