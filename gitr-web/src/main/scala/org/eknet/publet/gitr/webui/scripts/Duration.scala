package org.eknet.publet.gitr.webui.scripts

import java.util.concurrent.TimeUnit

/**
 * From `startMillis` until now.
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 29.05.12 10:03
 *
 */
case class Duration(startMillis: Long) {

  def distance: Long = System.currentTimeMillis() - startMillis
  def distance(unit: TimeUnit): Long = unit match {
    case TimeUnit.DAYS => TimeUnit.MILLISECONDS.toDays(distance)
    case TimeUnit.HOURS => TimeUnit.MILLISECONDS.toHours(distance)
    case TimeUnit.MINUTES => TimeUnit.MILLISECONDS.toMinutes(distance)
    case TimeUnit.SECONDS => TimeUnit.MILLISECONDS.toSeconds(distance)
    case TimeUnit.MICROSECONDS => TimeUnit.MILLISECONDS.toMicros(distance)
    case TimeUnit.NANOSECONDS => TimeUnit.MILLISECONDS.toNanos(distance)
  }

  def distanceAgo = {
    distance(TimeUnit.DAYS) match {
      case d if (d>0) => d +" days ago"
      case _ => distance(TimeUnit.HOURS) match {
        case h if (h>0) => h + " hours ago"
        case _ => distance(TimeUnit.MINUTES) match {
          case m if (m>0) => m + " minutes ago"
          case _=> distance(TimeUnit.SECONDS) match {
            case s if (s>5) => s+ " seconds ago"
            case _ => "moments ago"
          }
        }
      }
    }
  }
}
