package org.eknet.publet.auth

import org.eknet.publet.vfs.Path

trait AuthManager {

  def getUser(name: String): Option[User]

  def policyFor(username: String): Option[Policy]

}

