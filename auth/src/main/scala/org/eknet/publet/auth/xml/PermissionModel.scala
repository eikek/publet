package org.eknet.publet.auth.xml

import collection.mutable
import xml.{Node, Elem}
import org.eknet.publet.auth.Permission


/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 17.05.12 19:20
 */
case class PermissionModel(perm: String, repository: List[String], roles: List[String]) {

  def toXml: Elem = {
    <grant name={perm}>
      { for (repo <- repository) yield <on>{repo}</on> }
      { for (role <- roles) yield <to>{role}</to> }
    </grant>
  }

  def toPermissions: Map[String, List[Permission]] = {
    val map = mutable.Map[String, List[Permission]]()
    for (role <- roles) {
      val list = mutable.ListBuffer[Permission]()
      for (repo <- repository) {
        list.append(Permission(perm, Some(repo)))
      }
      if (list.isEmpty) {
        list.append(Permission(perm, None))
      }
      map.put(role, list.toList)
    }
    map.toMap
  }
}

object PermissionModel {

  def apply(node: Node): PermissionModel = {
    val perm = (node \ "@name").text
    val roles = (node \ "to").map(_.text).toList
    val repos = (node \ "on").map(_.text).toList
    PermissionModel(perm.toLowerCase, repos, roles)
  }
}
