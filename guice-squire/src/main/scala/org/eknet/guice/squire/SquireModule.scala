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
import com.google.inject.binder.{LinkedBindingBuilder, ScopedBindingBuilder, AnnotatedBindingBuilder}
import com.google.inject.multibindings.Multibinder
import SquireModule._
import com.google.inject.matcher.Matcher
import com.google.inject.spi.{TypeListener, TypeEncounter}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 05.11.12 21:32
 */
trait SquireModule {

  import SquireModule._

  protected def binder(): Binder

  def bind[A: Manifest]: SquireAnnotatedBindingBuilder[A] =
    new SquireAnnotatedBindingBuilder[A](binder().bind(typeLiteral[A]))

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

object SquireModule {

  def typeLiteral[A: Manifest]: TypeLiteral[A] = TypeLiteral.get(classFor[A])

  def classFor[A: Manifest] = manifest[A].erasure.asInstanceOf[Class[A]]

  def key[A: Manifest] = Key.get(typeLiteral[A])

  class SquireSetBindingBuilder[A: Manifest](val self: Multibinder[A]) {
    def withDuplicates = { self.permitDuplicates(); this }

    def add[B <: A: Manifest] = self.addBinding().to(classFor[A])
  }

  implicit def enrichMultibinder[A: Manifest](mb: Multibinder[A]) = new SquireSetBindingBuilder[A](mb)
  implicit def toMultibinder[A](eb: SquireSetBindingBuilder[A]) = eb.self
}


class SquireScopedBindingBuilder(val self: ScopedBindingBuilder) {

  import java.lang.annotation.{Annotation => JAnnotation}

  def as[A <: JAnnotation: Manifest]() {
    self.in(classFor[A])
  }
}

object SquireScopedBindingBuilder {

  implicit def enrichScopeBuilder(sb: ScopedBindingBuilder) = new SquireScopedBindingBuilder(sb)
  implicit def toScopeBuilder(eb: SquireScopedBindingBuilder) = eb.self

}

class SquireAnnotatedBindingBuilder[A: Manifest](val self: AnnotatedBindingBuilder[A]) {
  import java.lang.annotation.{Annotation => JAnnotation}

  def to[B <: A: Manifest]: SquireScopedBindingBuilder =
    new SquireScopedBindingBuilder(self.to(classFor[B]))

  def annotatedWith[B <: JAnnotation: Manifest]() =
    new SquireLinkingBindingBuilder[A](self.annotatedWith(classFor[B]))

  def annotatedWith(annot: JAnnotation) =
    new SquireLinkingBindingBuilder[A](self.annotatedWith(annot))
}

object SquireAnnotatedBindingBuilder {
  implicit def enrichAnnotationBindingBuilder[A: Manifest](ab: AnnotatedBindingBuilder[A]) =
    new SquireAnnotatedBindingBuilder[A](ab)

  implicit def toAnnotationBindingBuilder[A](eb: SquireAnnotatedBindingBuilder[A]) = eb.self

}

class SquireLinkingBindingBuilder[A: Manifest](val self: LinkedBindingBuilder[A]) {
  def to[B <: A: Manifest]: SquireScopedBindingBuilder =
    new SquireScopedBindingBuilder(self.to(classFor[B]))
}

object SquireLinkingBindingBuilder {

  implicit def enrichLinkingBuilder[A: Manifest](lb: LinkedBindingBuilder[A]) =
    new SquireLinkingBindingBuilder[A](lb)

  implicit def toLinkingBuilder[A](eb: SquireLinkingBindingBuilder[A]) = eb.self
}