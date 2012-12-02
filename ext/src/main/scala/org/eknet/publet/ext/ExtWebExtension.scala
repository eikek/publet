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

package org.eknet.publet.ext

import org.eknet.publet.web.scripts.WebScriptResource
import org.eknet.publet.vfs.{Resource, Path}
import grizzled.slf4j.Logging
import org.eknet.publet.vfs.util.{ClasspathContainer, MapContainer}
import com.google.inject.{Inject, Singleton, Provides}
import org.eknet.squaremail.{MailSender, DefaultMailSender, DefaultSessionFactory}
import com.google.common.eventbus.Subscribe
import org.eknet.publet.Publet
import org.eknet.publet.web.guice.{PubletStartedEvent, PubletBinding}
import org.eknet.publet.web.Config
import org.eknet.guice.squire.SquireModule

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 26.04.12 20:17
 */
@Singleton
class ExtWebExtension @Inject() (publet: Publet) extends Logging {
  val extScriptPath = Path("/publet/ext/scripts/")
  @Subscribe
  def onStartup(ev: PubletStartedEvent) {
    import org.eknet.publet.vfs.ResourceName._
    val muc = new MapContainer()
    muc.addResource(new WebScriptResource("captcha.png".rn, new CaptchaScript))
    muc.addResource(new WebScriptResource("sendMail.json".rn, new MailContact))
    muc.addResource(new WebScriptResource("myDataUpdate.json".rn, new MyDataScript))
    publet.mountManager.mount(extScriptPath, muc)

    val cont = new ClasspathContainer(base = "/org/eknet/publet/ext/includes")
    publet.mountManager.mount(Path("/publet/ext/includes/"), cont)
  }

}

class ExtraModule extends SquireModule with PubletBinding {

  def configure() {
    bind[ExtWebExtension].asEagerSingleton()

    bindDocumentation(docResource("extrasdoc.md", "captcha-example.png", "captcha-example1.png", "downloadTemplate.png"))
  }

  private[this] def docResource(names: String*) = names.map("org/eknet/publet/ext/doc/"+ _).map(Resource.classpath(_)).toList

  @Provides@Singleton
  def createDefaultMailer(config: Config): MailSender = {
    val sessionFactory = new DefaultSessionFactory(
      config("smtp.host").getOrElse("localhost"),
      config("smtp.port").getOrElse("-1").toInt,
      config("smtp.username").getOrElse(""),
      config("smtp.password").getOrElse("").toCharArray)

    new DefaultMailSender(sessionFactory)
  }

  override def toString = "Extras"
}