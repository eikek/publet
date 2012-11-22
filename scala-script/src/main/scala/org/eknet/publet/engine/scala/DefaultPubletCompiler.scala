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

import org.eknet.publet.Publet
import org.eknet.publet.vfs.{Path, ContentResource}
import tools.nsc.io.VirtualDirectory
import java.util.concurrent.{TimeUnit, ConcurrentHashMap}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 27.04.12 21:00
 */
class DefaultPubletCompiler(val publet: Publet,
                            val pathPrefix: String,
                            classPath: Option[String],
                            imports: List[String]) extends PubletCompiler {


  private[this] val lock = new ResourceLock
  private[this] val compiler = new ScriptCompiler(new VirtualDirectory("(memory)", None), classPath, imports)

  def evaluate(path: Path, resource: ContentResource) = {
    val r = (path / resource).asString
    lock.lock(r, 2, TimeUnit.MINUTES)
    try {
      val miniProject = MiniProject.find(path, publet, pathPrefix)
      val script = compiler.scriptLoader(miniProject, path, resource)
      Some(script)
    } finally {
      lock.release(r)
    }
  }

  private class ResourceLock {
    private[this] val map = new ConcurrentHashMap[String, String]()

    def lock(resource: String, timeout: Long, unit: TimeUnit) {
      while (map.putIfAbsent(resource, resource) != null) {
        synchronized { wait(unit.toMillis(timeout)) }
      }
    }

    def release(resource: String) {
      if (map.remove(resource) != null) {
        synchronized(notifyAll())
      }
    }
  }
}
