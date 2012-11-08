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

import com.google.inject._
import com.google.inject.multibindings.Multibinder
import com.google.inject.matcher.Matcher
import com.google.inject.spi.{TypeListener, TypeEncounter}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 05.11.12 21:32
 */
trait SquireBinder {

  import SquireBinder._

  def binder(): Binder

  def bind[A: Manifest]: AnnotatedBindingBuilder[A] =
    new AnnotatedBindingBuilder[A](binder().bind(typeLiteral[A]))

  def setOf[A: Manifest] =
    new SquireSetBindingBuilder[A](Multibinder.newSetBinder(binder(), typeLiteral[A]))

  def bindListener(matcher: Matcher[_ >: TypeLiteral[_]])(f: (TypeLiteral[_], TypeEncounter[_]) => Unit) {
    binder().bindListener(matcher, new TypeListener {
      def hear[I](literal: TypeLiteral[I], encounter: TypeEncounter[I]) {
        f(literal, encounter)
      }
    })
  }
}

object SquireBinder {
  import java.lang.annotation.{Annotation => JAnnotation}

  def typeLiteral[A: Manifest]: TypeLiteral[A] = TypeLiteral.get(classFor[A])

  def classFor[A: Manifest] = manifest[A].erasure.asInstanceOf[Class[A]]

  def key[A: Manifest] = Key.get(typeLiteral[A])

  def key[A: Manifest](annot: JAnnotation) = Key.get(classFor[A], annot)

  def key[A: Manifest](annotType: Class[_ <: JAnnotation]) = Key.get(classFor[A], annotType)

  class SquireSetBindingBuilder[A: Manifest](val self: Multibinder[A]) {
    def withDuplicates = { self.permitDuplicates(); this }

    def add[B <: A: Manifest] = self.addBinding().to(classFor[B])
  }

  implicit def enrichMultibinder[A: Manifest](mb: Multibinder[A]) = new SquireSetBindingBuilder[A](mb)
  implicit def toMultibinder[A](eb: SquireSetBindingBuilder[A]) = eb.self
}
