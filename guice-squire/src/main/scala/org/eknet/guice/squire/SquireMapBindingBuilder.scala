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

import com.google.inject.multibindings.{Multibinder, MapBinder}
import com.google.inject.Binder

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 26.11.12 20:20
 */
class SquireMapBindingBuilder[A: Manifest, B: Manifest](val self: MapBinder[A, B]) {

  def withDuplicates = { self.permitDuplicates(); this }
  def add(key: A) = new LinkedBindingBuilder[B](self.addBinding(key))

}

object SquireMapBindingBuilder {
  implicit def mapbbToSquireMapbb[A: Manifest, B: Manifest](binder: MapBinder[A,B]) = new SquireMapBindingBuilder[A, B](binder)
  implicit def toGuiceBinder[A, B](bb: SquireMapBindingBuilder[A, B]) = bb.self
}

class SquireMapBinderFactory[A: Manifest, B: Manifest](binder: Binder) {
  import java.lang.annotation.{Annotation => JAnnotation}
  import SquireBinder.typeLiteral
  import SquireBinder.classFor

  def by[C <: JAnnotation: Manifest]()
    = new SquireMapBindingBuilder[A, B](MapBinder.newMapBinder(binder, typeLiteral[A], typeLiteral[B], classFor[C]))

  def by(annot: JAnnotation)
    = new SquireMapBindingBuilder[A, B](MapBinder.newMapBinder(binder, typeLiteral[A], typeLiteral[B], annot))

}