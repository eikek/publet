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

import com.google.inject.{Key, Injector}
import com.google.inject.name.{Names, Named}
import SquireBinder._
import com.google.inject.util.Types

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 08.11.12 17:00
 */
trait LookupSquire {

  def injector: Injector

  def instance[A: Manifest] = new InstanceLookup[A]

  def setOf[A: Manifest] = new SetLookup[A]

  class SetLookup[A: Manifest] {
    import java.lang.annotation.{Annotation => JAnnotation}
    import java.util.{Set => JSet}
    import collection.JavaConversions._

    implicit def get: Set[A] = injector.getInstance(Key.get(Types.setOf(classFor[A]))).asInstanceOf[JSet[A]].toSet

    def annotatedWith[J <: JAnnotation: Manifest]: Set[A] =
      injector.getInstance(Key.get(Types.setOf(classFor[A]), classFor[J])).asInstanceOf[JSet[A]].toSet

    def annotatedWith(annot: JAnnotation): Set[A] =
      injector.getInstance(Key.get(Types.setOf(classFor[A]), annot)).asInstanceOf[JSet[A]].toSet

    def named(name: String) = annotatedWith(Names.named(name))
  }

  class InstanceLookup[A: Manifest] {

    import java.lang.annotation.{Annotation => JAnnotation}

    implicit def get = injector.getInstance(classFor[A])

    def annotatedWith(name: JAnnotation): A = injector.getInstance(key[A](name))

    def annotatedWith[J <: JAnnotation: Manifest]: A = injector.getInstance(key[A](classFor[J]))

    /**
     * Uses the Guice Injector to retrieve an instance of the
     * given type named with the given string.
     *
     * @param name
     * @return
     */
    def named(name: String): A = annotatedWith(Names.named(name))
  }
}
