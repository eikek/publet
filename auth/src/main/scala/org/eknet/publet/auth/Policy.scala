package org.eknet.publet.auth


/**
 * A policy is a set of permissions reduced to a
 * single user.
 *
 */
trait Policy {

  def getRoles: Set[String]

  def getPermissions: Set[String]

}

object Policy {

  lazy val Empty: Policy = new Policy {
    def getRoles = Set()
    def getPermissions = Set()
  }

}