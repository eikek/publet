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

package org.eknet.publet.ext.jmx

import org.eknet.guice.squire.SquireModule
import org.eknet.publet.web.guice.PubletBinding
import com.google.inject.spi.{TypeEncounter, TypeListener, InjectionListener}
import javax.management.{JMException, DynamicMBean, JMX}
import com.google.inject.matcher.AbstractMatcher
import com.google.inject.TypeLiteral
import org.eknet.publet.vfs.Resource
import grizzled.slf4j.Logging

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 24.11.12 16:12
 */
class JmxConnectorModule extends SquireModule with PubletBinding with Logging {
  def configure() {
    bind[JmxService].asEagerSingleton()
    bindListener(new MBeanMatcher, new TypeListener {
      def hear[I](`type`: TypeLiteral[I], encounter: TypeEncounter[I]) {
        encounter.register(new InjectionListener[I] {
          def afterInjection(injectee: I) {
            registerMBean(injectee.asInstanceOf[AnyRef])
          }
        })
      }
    })
    bindDocumentation(List(Resource.classpath("org/eknet/publet/ext/doc/jmxdoc.md")))
  }

  override def toString = "JMX Connector"

  private[this] def registerMBean(injectee: AnyRef) {
    try {
      JmxService.registerMBean(injectee)
    }
    catch {
      case e: JMException => {
        error("Error registering MBean '"+injectee+"'!", e)
      }
    }
  }
}

class MBeanMatcher extends AbstractMatcher[TypeLiteral[_]] {
  def matches(t: TypeLiteral[_]) = {
    val cls = t.getRawType
    isStandardMBean(cls) || isDynamicMBean(cls) || isMxBean(cls)
  }

  private[this] def isMxBean(cls: Class[_]) = {
    cls.getInterfaces.exists(i => JMX.isMXBeanInterface(i))
  }

  private[this] def isStandardMBean(cls: Class[_]) =
    cls.getInterfaces.exists(i => i.getSimpleName.endsWith("MBean"))

  private[this] def isDynamicMBean(cls: Class[_]) =
    classOf[DynamicMBean].isAssignableFrom(cls)
}