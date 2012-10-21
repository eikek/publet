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

package org.eknet.publet.auth

import org.apache.shiro.crypto.hash._
import org.apache.shiro.authc.credential.{DefaultPasswordService, PasswordService}
import org.apache.shiro.crypto.hash.format.Shiro1CryptFormat

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 21.10.12 02:29
 */
trait PasswordServiceProvider {

  def forAlgorithm(alg: Algorithm.Value): PasswordService

}

object PasswordServiceProvider {
  /**
   * Creates a new [[org.apache.shiro.authc.credential.PasswordService]] using the
   * given algorithm name. Use the constants defined in shiros concrete hash classes (extend
   * [[org.apache.shiro.crypto.hash.SimpleHash]]), like [[org.apache.shiro.crypto.hash.Md5Hash]].
   *
   * @param algorithm
   * @return
   */
  def newPasswordService(algorithm: String) = {
    val ps = new DefaultPasswordService
    val hs = new DefaultHashService()
    val hf = new Shiro1CryptFormat
    hs.setHashAlgorithmName(algorithm)
    hs.setHashIterations(DefaultPasswordService.DEFAULT_HASH_ITERATIONS)
    hs.setGeneratePublicSalt(true)
    ps.setHashService(hs)
    ps.setHashFormat(hf)
    ps
  }

}
object Algorithm extends Enumeration {
  val md2 = Value(Md2Hash.ALGORITHM_NAME)
  val md5 = Value(Md5Hash.ALGORITHM_NAME)
  val sha1 = Value(Sha1Hash.ALGORITHM_NAME)
  val sha256 = Value(Sha256Hash.ALGORITHM_NAME)
  val sha384 = Value(Sha384Hash.ALGORITHM_NAME)
  val sha512 = Value(Sha512Hash.ALGORITHM_NAME)
}