package org.eknet.publet.web.filter

import javax.servlet.http.{HttpServletResponse, HttpServletRequest}

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 05.04.12 14:04
 *
 */
class SuperFilter(val filters: List[Filter]) extends Filter {
  
  override def handle(req: HttpServletRequest, resp: HttpServletResponse) = {
    def apply(f: List[Filter]): Boolean = {
      f match {
        case List() => false
        case a :: tail => a.handle(req, resp) match {
          case true => true
          case false => apply(tail)
        }
      }
    }
    apply(filters)
  }
  
}

object SuperFilter {

  def apply() {
    new SuperFilter(
        PushFilesFilter ::
        EditFilter ::
        PublishFilter ::
      Nil
    )
  }
}
