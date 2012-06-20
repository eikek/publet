/*
 * Copyright 2012 Eike Kettner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.eknet.publet.gitr.web.scripts

import java.net.URL
import org.apache.shiro.crypto.hash.Md5Hash

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 03.06.12 12:12
 */
object Gravatar {

  private val urlBase = "http://www.gravatar.com/avatar/"
  private val secureBase = "https://secure.gravatar.com/avatar/"

  def imageUrl(secure: Boolean)(email: String, extension: Option[String] = None, size: Option[Int] = None, default: Option[Defaults.Value] = None): URL = {
    val md5 = new Md5Hash(email.trim.toLowerCase).toHex
    val s = size map  { s => "s="+s }
    val d = default map { d => "d="+d.toString }
    val opts = List(s, d) collect ({ case o if (o.isDefined) => o.get.toString }) mkString ("&")
    val ext = extension.getOrElse("jpg")
    val base = if (secure) secureBase else urlBase
    new URL(base+ md5 +"."+ ext + (if (opts.isEmpty) "" else "?"+opts))
  }

  object Defaults extends Enumeration {
    val d404 = Value("404")
    val mm = Value("mm")
    val identicon = Value("identicon")
    val monster = Value("monsterid")
    val wavatar = Value("wavatar")
    val retro = Value("retro")
  }
}
