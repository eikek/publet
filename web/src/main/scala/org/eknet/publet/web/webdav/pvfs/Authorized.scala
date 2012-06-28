package org.eknet.publet.web.webdav.pvfs

import com.bradmcevoy.http.{Auth, Request, Resource}
import com.bradmcevoy.http.Request.Method
import org.eknet.publet.web.shiro.Security
import org.eknet.publet.vfs.Path
import org.eknet.publet.web.PubletWebContext
import org.eknet.publet.web.filter.AuthzFilter

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 28.06.12 19:07
 */
trait Authorized extends Resource {

  /**
   * Checks permission for the current request uri.
   *
   * @param request
   * @param method
   * @param auth
   * @return
   */
  override def authorise(request: Request, method: Method, auth: Auth): Boolean = {
    if (method.isWrite) {
      Security.hasWritePermission(PubletWebContext.applicationPath)
    } else {
      AuthzFilter.hasAccessToResource(PubletWebContext.applicationUri)
    }
  }
}
