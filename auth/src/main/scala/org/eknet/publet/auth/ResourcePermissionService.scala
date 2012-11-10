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

package org.eknet.publet.auth

import org.eknet.publet.Publet
import com.google.inject.{Inject, Singleton}
import org.eknet.publet.auth.store.{ResourcePatternDef, DefaultAuthStore}
import org.eknet.publet.vfs.Path
import ResourceAction._
import org.apache.shiro.SecurityUtils

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 08.11.12 17:38
 */
@Singleton
class ResourcePermissionService @Inject() (publet: Publet, authm: DefaultAuthStore) extends PermissionBuilder {

  private def permString(action: Action, rd: ResourcePatternDef, path: Path): String = {
    if (rd.by.isEmpty) resource action(action) on path.asString
    else resource grant(rd.by) on path.asString
  }

  private def hasReadPermission(path: Path, f: String=>Boolean): Boolean = {
    val mounted = publet.mountManager.resolveMount(path)
    mounted match {
      case Some((p, c: Authorizable)) if (c.isAuthorized(read)) => true
      case _ => {
        val restricted = authm.restrictedResources.filter(rd => rd.pattern.matches(path.asString) && rd.on.contains(read))
        val ok = restricted.foldLeft(true)((b, rd) => b && f(permString(read, rd, path)))
        if (ok && !restricted.isEmpty) true
        else {
          val anon = authm.anonPatterns.foldLeft(false)((b, op) => if (b) true else op.matches(path.asString))
          if (anon) true
          else f(resource action(read) on path.asString)
        }
      }
    }
  }

  /**
   * Returns whether the current subject is allowed to read the resource at the
   * given path.
   *
   * @param path
   * @return
   */
  def isReadPermitted(path: Path) = hasReadPermission(path, perm => SecurityUtils.getSubject.isPermitted(perm))

  /**
   * Checks whether the current subject is allowed to read the resource at the
   * given path. If access is not granted an exception is thrown.
   *
   * @param path
   * @return
   */
  def checkRead(path: Path) = hasReadPermission(path, perm => {
    SecurityUtils.getSubject.checkPermission(perm)
    true
  })


  private def hasWritePermission(path: Path, f: String => Boolean): Boolean = {
    val mounted = publet.mountManager.resolveMount(path)
    mounted match {
      case Some((p, c: Authorizable)) if (c.isAuthorized(write)) => true
      case _ => {
        val restricted = authm.restrictedResources.filter(rd => rd.pattern.matches(path.asString) && rd.on.contains(write))
        val ok = restricted.foldLeft(true)((b, rd) => b && f(permString(write, rd, path)))
        if (ok && !restricted.isEmpty) true
        else {
          f(resource action(write) on path.asString)
        }
      }
    }
  }

  /**
   * Returns whether the current subject is allowed to write to the resource
   * at the given path.
   *
   * @param path
   * @return
   */
  def isWritePermitted(path: Path) = hasWritePermission(path, perm => SecurityUtils.getSubject.isPermitted(perm))

  /**
   * Checks whether the current subject is allowed to write to the resource
   * at the given path. If access is not granted, an exception is thrown.
   *
   * @param path
   * @return
   */
  def checkWrite(path: Path) = hasWritePermission(path, perm => {
    SecurityUtils.getSubject.checkPermission(perm)
    true
  })

}
