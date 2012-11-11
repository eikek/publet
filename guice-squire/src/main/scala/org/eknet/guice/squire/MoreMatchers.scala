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

import com.google.inject.matcher.{Matcher, AbstractMatcher, Matchers=>GMatchers}
import com.google.inject.TypeLiteral

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 11.11.12 14:05
 */
object MoreMatchers {
  import SquireBinder._

  def ofClass[A: Manifest] :Matcher[TypeLiteral[_]] = new TypeListenerClassMatcher(classFor[A])

  def literalMatcher(delegate: Matcher[Class[_]]): Matcher[TypeLiteral[_]] = new ClassMatcher(delegate)

  def subclassOf[A: Manifest] :Matcher[TypeLiteral[_]] = literalMatcher(GMatchers.subclassesOf(classFor[A]))

  def inSubpackage(packageName: String) :Matcher[TypeLiteral[_]] = literalMatcher(GMatchers.inSubpackage(packageName))

  def or[A](matchers: Matcher[A]*): Matcher[A] = new OrMatcher[A](matchers: _*)
  def and[A](matchers: Matcher[A]*): Matcher[A] = new AndMatcher[A](matchers: _*)

  private class TypeListenerClassMatcher(aclass: Class[_]) extends AbstractMatcher[TypeLiteral[_]] {
    def matches(t: TypeLiteral[_]) = aclass == t.getRawType
  }

  private class ClassMatcher(val self: Matcher[Class[_]]) extends AbstractMatcher[TypeLiteral[_]] with Proxy {
    def matches(t: TypeLiteral[_]) = self.matches(t.getRawType)
  }

  private abstract class CompositeMatcher[A](foldEl: Boolean, matchers: Matcher[A]*) extends AbstractMatcher[A] {
    def matches(t: A) = matchers.foldLeft(foldEl)(foldFun(t)_)
    def foldFun(t:A)(b: Boolean, m: Matcher[A]): Boolean
  }

  private class OrMatcher[A](matchers: Matcher[A]*) extends CompositeMatcher[A](false, matchers: _*) {
    def foldFun(t: A)(b: Boolean, m: Matcher[A]) = if (b) true else m.matches(t)
  }
  private class AndMatcher[A](matchers: Matcher[A]*) extends CompositeMatcher[A](true, matchers: _*) {
    def foldFun(t: A)(b: Boolean, m: Matcher[A]) = if (!b) false else m.matches(t)
  }
}
