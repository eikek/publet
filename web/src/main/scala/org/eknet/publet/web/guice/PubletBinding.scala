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

package org.eknet.publet.web.guice

import com.google.inject.multibindings.Multibinder
import org.apache.shiro.realm.Realm
import com.google.inject.{Binder, Key, AbstractModule}
import org.eknet.publet.web.WebExtension
import org.eknet.publet.web.req.RequestHandlerFactory
import com.google.inject.binder.{AnnotatedBindingBuilder, LinkedBindingBuilder}
import org.eknet.publet.auth.user.UserStore
import org.eknet.publet.auth.repository.RepositoryStore
import org.eknet.publet.auth.resource.ResourceConstraintStore

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 15.10.12 13:03
 * 
 */
trait PubletBinding {

  class HBindingBuilder[A](val bb: LinkedBindingBuilder[A]) {
    def toType[B <: A: Manifest] = bb.to(manifest[B].erasure.asInstanceOf[Class[B]])
  }
  implicit def bb2Hbb[A](bb: LinkedBindingBuilder[A]): HBindingBuilder[A] = new HBindingBuilder[A](bb)
  implicit def hbb2bb[A](hbb: HBindingBuilder[A]): LinkedBindingBuilder[A] = hbb.bb

  class HModule(binder: Binder) {

    def bindRequestHandler = setBind[RequestHandlerFactory]
    def bindExtension = setBind[WebExtension]
    def bindRealm = setBind[Realm]
    def bindUserStore = setBind[UserStore]
    def bindRepositoryStore = setBind[RepositoryStore]
    def bindConstraintStore = setBind[ResourceConstraintStore]

    def bindEagerly[A: Manifest]() {
      val clazz = manifest[A].erasure.asInstanceOf[Class[A]]
      binder.bind(clazz).asEagerSingleton()
    }


    def setBind[T: Manifest] = new HBindingBuilder[T](Multibinder
      .newSetBinder(binder, manifest[T].erasure.asInstanceOf[Class[T]]).addBinding())

    def set[T: Manifest]: AnnotatedBindingBuilder[T] = binder.bind(manifest[T].erasure.asInstanceOf[Class[T]])
    def set[T](key: Key[T]): LinkedBindingBuilder[T] = binder.bind(key)
  }

  implicit def toHBinder(b:Binder) = new HModule(b)
}


