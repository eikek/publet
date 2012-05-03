package org.eknet.publet.auth

import org.eknet.publet.vfs.Path

trait AuthManager {

  /**
   * Specifies whether this manager is active.
   *
   * @return
   */
  def isActive: Boolean

  def getUser(name: String): Option[User]

  def policyFor(username: String): Policy

  def policyFor(user: User): Policy

  def urlMappings: List[(String, String)]
}

