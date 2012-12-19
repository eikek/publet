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

package org.eknet.publet.vfs.util

import org.eknet.publet.vfs.{ResourceName, Resource}

/**
 * @param classpathResource the resource name within the classpath
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 26.11.12 21:48
 */
class ClasspathResource(val classpathResource: String, classLoader: Option[ClassLoader], name: Option[ResourceName]) extends ForwardingContentResource {

  protected lazy val delegate = {
    val cl = classLoader.getOrElse(Thread.currentThread().getContextClassLoader)
    val clr = Option(cl.getResource(classpathResource)).getOrElse(sys.error("Resource '"+classpathResource+"' does not exist"))
    if (name.isDefined)
      new UrlResource(clr, name.get)
    else
      new UrlResource(clr)
  }

}
