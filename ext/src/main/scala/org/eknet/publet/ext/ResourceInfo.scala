package org.eknet.publet.ext

import counter.CounterExtension
import org.eknet.publet.web.{PubletWebContext, PubletWeb}
import org.eknet.publet.vfs.Path
import java.text.DateFormat
import java.util
import org.eknet.publet.partition.git.GitFile
import org.eknet.publet.auth.UserProperty

/**
 * A helper class that defines method for retrieving information to
 * a resource -- either given explicitely by uri, or using the one
 * of the current request.
 *
 * This is intended for use within templates.
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 23.06.12 12:31
 */
object ResourceInfo {

  /**
   * Returns the number of accesses to the resource at the given uri.
   *
   * Note, that the [[org.eknet.publet.ext.counter.CounterExtension]] must
   * be active, otherwise `0` is returned.
   *
   * @param uri
   * @return
   */
  def getAccessCount(uri: String): Long = {
    CounterExtension.serviceOption map (_.getPageCount(uri)) getOrElse (0L)
  }

  /**
   * Returns the number of accesses to the resource the current request
   * points to.
   *
   * Note, that the [[org.eknet.publet.ext.counter.CounterExtension]] must
   * be active, otherwise `0` is returned.
   *
   * @return
   */
  def getAccessCount: Long = getAccessCount(PubletWebContext.fullUrl)

  /**
   * Returns the point in time of last access to the resource at the given
   * uri.
   *
   * Note, that the [[org.eknet.publet.ext.counter.CounterExtension]] must
   * be active, otherwise `0` is returned.
   *
   * @param uri
   * @return
   */
  def getLastAccess(uri: String): Long = {
    CounterExtension.serviceOption map (_.getLastAccess(uri)) getOrElse (0L)
  }

  /**
   * Returns the point in time of the last access to the resource the current
   * request points to.
   *
   * Note, that the [[org.eknet.publet.ext.counter.CounterExtension]] must
   * be active, otherwise `0` is returned.
   *
   * @return
   */
  def getLastAccess:Long = getLastAccess(PubletWebContext.fullUrl)

  /**
   * Returns the timestamp of the last access to the resource at the given uri
   * formatted using [[java.text.DateFormat.MEDIUM]] for both date and time. The
   * locale is taken from the current request.
   *
   * Note, that the [[org.eknet.publet.ext.counter.CounterExtension]] must
   * be active, otherwise an empty string is returned.
   *
   *@param uri
   * @return
   */
  def getLastAccessString(uri: String): String = {
    CounterExtension.serviceOption map (_.getLastAccessString(uri)) getOrElse ("")
  }

  /**
   * Returns the timestamp of the last access to the resource the current request
   * points to formatted using [[java.text.DateFormat.MEDIUM]] for both date
   * and time. The locale is taken from the current request.
   *
   * Note, that the [[org.eknet.publet.ext.counter.CounterExtension]] must
   * be active, otherwise an empty string is returned.
   *
   * @return
   */
  def getLastAccessString: String = getLastAccessString(PubletWebContext.fullUrl)

  private def findSource(path: Path) = PubletWeb.publet.findSources(path).headOption

  /**
   * Returns the point in time of the last modification to the resource at the given
   * uri. Since not all resources support this information, it is returned as optional.
   *
   * @param uri
   * @return
   */
  def getLastModified(uri: String): Option[Long] =
    findSource(Path(uri)).flatMap(_.lastModification)

  /**
   * Returns the point in time of the last modification to the resource the current
   * request points to. Since not all resources support this information, it is
   * returned as optional.
   *
   * @return
   */
  def getLastModified: Option[Long] = getLastModified(PubletWebContext.applicationUri)

  /**
   * Returns the timestamp of the last modification of the resource at the given uri. It
   * is formatted using [[java.text.DateFormat.MEDIUM]] for both date and time. The locale
   * is taken from the current request. Since not all resources support this information,
   * it is returned as optional.
   *
   * @param uri
   * @return
   */
  def getLastModifiedString(uri: String): Option[String] = {
    val df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, PubletWebContext.getLocale)
    getLastModified(uri) map  (time => df.format(new util.Date(time)))
  }

  /**
   * Returns the timestamp of the last modification of the resource the current request
   * points to. It is formatted using [[java.text.DateFormat.MEDIUM]] for both date and
   * time. The locale is taken from the current request. Since not all resources support
   * this information, it is returned as optional.
   *
   * @return
   */
  def getLastModifiedString: Option[String] = getLastModifiedString(PubletWebContext.applicationUri)

  /**
   * Returns the name of the person who last authored the resource at the given uri. This
   * is only possible for resources backed by a git repository.
   *
   * @param uri
   * @return
   */
  def getLastAuthor(uri: String): Option[String] = {
    findSource(Path(uri))
      .collect({ case r: GitFile => r})
      .flatMap(_.lastAuthor.map(_.getName))
  }

  /**
   * Returns the name of the person who last authored the resource the current request points to.
   * This is only possible for resources backed by a git repository.
   *
   * @return
   */
  def getLastAuthor: Option[String] = getLastAuthor(PubletWebContext.applicationUri)

  /**
   * Returns the login of the person who last authored the resource at the given uri.
   * This is only possible for resources backed by a git repository. The email of the
   * author is used to lookup the user in the user database.
   *
   * @param uri
   * @return
   */
  def getLastAuthorLogin(uri: String): Option[String] = {
    val email = findSource(Path(uri))
      .collect({ case r: GitFile => r})
      .flatMap(_.lastAuthor.map(_.getEmailAddress))

    email.flatMap (mail => {
      PubletWeb.authManager
        .getAllUser
        .find(_.getProperty(UserProperty.email).exists(_ == mail))}
    ).map(_.login)
  }

  /**
   * Returns the login of the person who last authored the resource the current request
   * points to.
   *
   * This is only possible for resources backed by a git repository. The email of the
   * author is used to lookup the user in the user database.
   *
   * @return
   */
  def getLastAuthorLogin: Option[String] = getLastAuthorLogin(PubletWebContext.applicationUri)
}
