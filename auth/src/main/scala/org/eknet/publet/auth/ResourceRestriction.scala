package org.eknet.publet.auth

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 10.05.12 14:43
 */
case class ResourceRestriction(
    repository: String,
    pattern: String,
    permissions: Set[String]
)
