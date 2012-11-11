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

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import com.google.inject._
import java.awt.event.{ActionEvent, ActionListener}
import com.google.inject.multibindings.Multibinder
import com.google.inject.util.Types
import com.google.inject.matcher.{Matcher, Matchers}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 08.11.12 13:39
 */
class SetBindingTest extends FunSuite with ShouldMatchers {
  import collection.JavaConversions._

  test ("add impl") {
    val inj = Guice.createInjector(new ServiceAModule)
    val impls = inj.getInstance(Key.get(Types.setOf(classOf[ServiceA]))).asInstanceOf[java.util.Set[ServiceA]]
    impls.toSet should have size (2)
  }


  class ServiceAModule extends SquireModule {
    def configure() {

      Multibinder.newSetBinder(binder(), classOf[ServiceA]).addBinding().to(classOf[ServicAImpl])
      setOf[ServiceA].add[ServicAImpl]
    }
  }

  test ("multi binds") {
    val inj = Guice.createInjector(new ServiceABModule)
    val a = inj.getInstance(classOf[ServiceA])
    val b = inj.getInstance(classOf[ServiceB])
    a should be (b)
  }

  test ("InjectionListener registration for any") {
    val module = new InjectionListenerModule(Matchers.any())
    val inj = Guice.createInjector(module)
    inj.getInstance(classOf[ServiceA])
    inj.getInstance(classOf[ServiceB])
    module.set should have size (3)
  }

  test ("InjectionListener for specific class") {
    val module = new InjectionListenerModule(MoreMatchers.ofClass[ServicAImpl])
    val inj = Guice.createInjector(module)
    inj.getInstance(classOf[ServiceA])
    inj.getInstance(classOf[ServiceB])
    module.set should have size (1)
  }

  class InjectionListenerModule(matcher: Matcher[_ >: TypeLiteral[_]]) extends SquireModule {
    val set = collection.mutable.Set[String]()
    def configure() {
      addInjectionListener(matcher, (injectee => {
        set.add("InjectionListener:" +injectee)
      }))
      bind[ServiceA].to[ServicAImpl].in(Scopes.SINGLETON)
      bind[ServiceB].to[ServicBImpl].in(Scopes.SINGLETON)
    }
  }

  class ServiceABModule extends SquireModule {
    def configure() {
      bind[ServiceAB].in(Scopes.SINGLETON)
      bind[ServiceA].to[ServiceAB]
      bind[ServiceB].to[ServiceAB]
    }
  }
}

trait ServiceA
trait ServiceB

class ServicAImpl extends ServiceA
class ServicBImpl extends ServiceB
class ServiceAB extends ServiceA with ServiceB