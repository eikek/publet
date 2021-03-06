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

package org.eknet.publet.engine.scala

import org.eknet.publet.vfs.{Path, ContentResource}


/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 27.04.12 19:41
 */
trait PubletCompiler {

  /**
   * Compiles and executes the given scala script.
   *
   * The compiler is configured with the current classpath of
   * the application joint with the class path of the root
   * project and the project the given resource belongs to.
   * This is the closest parent project folder that is found
   * starting from the path at the given resource.
   *
   *
   * @param resource
   * @return
   */
  def evaluate(path: Path, resource: ContentResource): Option[ScalaScript]

  /**
   * Assumes that `resource` is a scala source file that contains a class/trait. The
   * source files is compiled and the class is loaded. The class name is derived
   * from the resource name.
   *
   * @param path
   * @param resource
   * @return
   */
  def loadClass(path: Path, resource: ContentResource): Class[_]
}
