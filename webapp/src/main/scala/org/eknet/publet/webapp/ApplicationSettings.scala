package org.eknet.publet.webapp

import com.typesafe.config.{ConfigException, Config}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 21.05.13 21:58
 */
class ApplicationSettings(val config: Config) {

  val highlightTheme = withDefault("default") {
    config.getString("highlight-theme")
  }

  val assetGroups = withDefault("default") {
    config.getString("asset-groups")
  }

  private def withDefault[A](fallback: A)(f: => A) = {
    try f catch {
      case e: ConfigException.Missing => fallback
    }
  }
}
