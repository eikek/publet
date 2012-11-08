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

import com.google.inject.binder.{AnnotatedBindingBuilder => GAnnotatedBindingBuilder}
import SquireBinder._

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 08.11.12 14:49
 */
class AnnotatedBindingBuilder[A: Manifest](val self: GAnnotatedBindingBuilder[A]) extends Proxy {
  import java.lang.annotation.{Annotation => JAnnotation}

  def to[B <: A: Manifest]: ScopedBindingBuilder =
    new ScopedBindingBuilder(self.to(classFor[B]))

  def annotatedWith[B <: JAnnotation: Manifest]() =
    new LinkedBindingBuilder[A](self.annotatedWith(classFor[B]))

  def annotatedWith(annot: JAnnotation) =
    new LinkedBindingBuilder[A](self.annotatedWith(annot))
}

object AnnotatedBindingBuilder {
  implicit def enrichAnnotationBindingBuilder[A: Manifest](ab: GAnnotatedBindingBuilder[A]) =
    new AnnotatedBindingBuilder[A](ab)

  implicit def toAnnotationBindingBuilder[A](eb: AnnotatedBindingBuilder[A]) = eb.self
}