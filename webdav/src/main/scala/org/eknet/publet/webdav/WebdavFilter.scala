package org.eknet.publet.webdav

import javax.servlet._
import org.eknet.publet.web._
import ref.WeakReference
import org.eknet.publet.Publet
import io.milton.servlet.{FilterConfigWrapper, DefaultMiltonConfigurator, MiltonFilter}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 25.06.12 21:02
 */
class WebdavFilter(publet: Publet) extends MiltonFilter with PubletRequestWrapper {


  override def init(filterConfig: FilterConfig) {
    super.init(filterConfig) //this is a must to properly initialize servletContext private member

    val mainResourceFactory = new WebdavResourceFactory(publet, filterConfig.getServletContext.getContextPath)
    this.configurator = new DefaultMiltonConfigurator {
      builder.setMainResourceFactory(mainResourceFactory)
    }
    val config = new FilterConfigWrapper(filterConfig)
    this.httpManager = this.configurator.configure(config)
  }

}
