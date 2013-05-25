package org.eknet.publet.webapp.assets

import org.eknet.publet.content.{ContentType, Content}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 13.05.13 18:38
 */
case class Asset(resource: Content, target: Option[String] = None, compress: Boolean = true, merge: Boolean = true, group: String = "") {

  def inGroup(name: String) = copy(group = name)

  /** Specifies to not compress the file (only applicable for js or css) */
  def noCompress = copy(compress = false)

  /** Specifies a explicit relative path to put the resource into */
  def into(target: String) = copy(target = Some(target))

  /**
   * Specifies to not merge this resource when requesting a
   * compressed and merged file. Only applicable for js and
   * css resources.
   */
  def noMerge = copy(merge = false)
}
