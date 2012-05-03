package org.eknet.publet.auth

import org.eknet.publet.vfs.Path

/**
 * A policy is a set of permissions reduced to a
 * single user.
 *
 */
trait Policy {

  /**
   * Returns all permissions associated to a user.
   *
   * @return
   */
  def permissions: Set[PermissionRule]

  def stringPermissions = permissions.flatMap(_.perms).toSet
}

object Policy {

  lazy val empty: Policy = new Policy {
    def permissions = Set()
  }

  def apply(perms: Set[PermissionRule]):Policy = new Policy {
    val permissions = perms
  }
}