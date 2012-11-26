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
import com.google.inject.multibindings.{MapBinder, Multibinder}
import com.google.inject.matcher.Matcher
import com.google.inject.spi.{InjectionListener, TypeEncounter}
import java.lang.reflect.{Type, ParameterizedType}
import com.google.inject.internal.MoreTypes

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

  def annotateSetOf[A: Manifest] = new SquireSetBinderFactory[A](binder())

  def mapOf[A: Manifest, B: Manifest] =
    new SquireMapBindingBuilder[A, B](MapBinder.newMapBinder(binder(), typeLiteral[A], typeLiteral[B]))

  def annoateMapOf[A: Manifest, B: Manifest] = new SquireMapBinderFactory[A, B](binder())

  def bindTypeListener(matcher: Matcher[_ >: TypeLiteral[_]], listener: (TypeLiteral[_], TypeEncounter[_]) => Unit) {
    binder().bindListener(matcher, new TypeListenerFunction(listener))
  }

  def addInjectionListener(matcher: Matcher[_ >: TypeLiteral[_]], listener:(Any)=>Unit) {
    bindTypeListener(matcher, (literal, encounter) => {
      val il: InjectionListener[AnyRef] = new InjectionListenerFun[AnyRef](listener)
      encounter.register(il.asInstanceOf[InjectionListener[_ >: Any]])
    })
  }
}

object SquireBinder {
  import java.lang.annotation.{Annotation => JAnnotation}

  def typeLiteral[A: Manifest]: TypeLiteral[A] = {
    val pt = resolveManifest(manifest[A])
    TypeLiteral.get(pt).asInstanceOf[TypeLiteral[A]]
  }


  private[this] def resolveManifest[A: Manifest]: Type = {
    val mf = manifest[A]
    mf.typeArguments match {
      case Nil => mf.erasure.asInstanceOf[Type]
      case list => new MoreTypes.ParameterizedTypeImpl(null, mf.erasure, list.map(resolveManifest(_)): _*)
    }
  }

  def classFor[A: Manifest] = manifest[A].erasure.asInstanceOf[Class[A]]

  def key[A: Manifest] = Key.get(typeLiteral[A])

  def key[A: Manifest](annot: JAnnotation) = Key.get(classFor[A], annot)

  def key[A: Manifest](annotType: Class[_ <: JAnnotation]) = Key.get(classFor[A], annotType)

}
