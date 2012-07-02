/*
 * Copyright 2011 gitblit.com.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
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
/*
 * Copyright 2012 Eike Kettner
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
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

package org.eknet.publet.server

import java.io.{FileOutputStream, FileInputStream, File}
import java.security.{KeyStore, KeyPairGenerator, SecureRandom, Security}
import javax.security.auth.x500.X500Principal
import java.util
import java.math.BigInteger
import util.concurrent.TimeUnit
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import org.bouncycastle.cert.jcajce.{JcaX509CertificateConverter, JcaX509v3CertificateBuilder}
import org.bouncycastle.asn1.x500.X500NameBuilder
import org.bouncycastle.asn1.x500.style.BCStyle

/**
 * This code is taken with great thanks from the gitblit project (http://gitblit.com)
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 02.07.12 22:55
 */
object MakeCertificate {
  private val BC = org.bouncycastle.jce.provider.BouncyCastleProvider.PROVIDER_NAME

  private val oneDay = TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS)
  private val oneYear = TimeUnit.MILLISECONDS.convert(365, TimeUnit.DAYS)
  
  def generateSelfSignedCertificate(hostname: String, keystore: File, keystorePassword: String, info: String) {
    try {
      Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider())

      val kpGen = KeyPairGenerator.getInstance("RSA", "BC")
      kpGen.initialize(1024, new SecureRandom())
      val pair = kpGen.generateKeyPair()

      // Generate self-signed certificate
      val principal = new X500Principal(info)

      val notBefore = new util.Date(System.currentTimeMillis() - oneDay)
      val notAfter = new util.Date(System.currentTimeMillis() + (10 * oneYear))
      val serial = BigInteger.valueOf(System.currentTimeMillis())

      val certGen = new JcaX509v3CertificateBuilder(principal, serial, notBefore, notAfter, principal, pair.getPublic)
      val sigGen = new JcaContentSignerBuilder("SHA256WithRSAEncryption").setProvider(BC).build(pair.getPrivate)
      val cert = new JcaX509CertificateConverter().setProvider(BC)
        .getCertificate(certGen.build(sigGen))
      cert.checkValidity(new util.Date())
      cert.verify(cert.getPublicKey)

      // Save to keystore
      val store = KeyStore.getInstance("JKS")
      if (keystore.exists()) {
        val fis = new FileInputStream(keystore)
        store.load(fis, keystorePassword.toCharArray)
        fis.close()
      } else {
        store.load(null)
      }
      store.setKeyEntry(hostname, pair.getPrivate, keystorePassword.toCharArray, Array( cert ))
      val fos = new FileOutputStream(keystore)
      store.store(fos, keystorePassword.toCharArray)
      fos.close()
    } catch {
      case t:Throwable => t.printStackTrace()
      throw new RuntimeException("Failed to generate self-signed certificate!", t)
    }
  }

  def generateSelfSignedCertificate(hostname: String, keystore: File, keystorePassword: String) {
    Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider())

    val kpGen = KeyPairGenerator.getInstance("RSA", "BC")
    kpGen.initialize(1024, new SecureRandom())
    val pair = kpGen.generateKeyPair()

    // Generate self-signed certificate
    val builder = new X500NameBuilder(BCStyle.INSTANCE)
    builder.addRDN(BCStyle.OU, "publet")
    builder.addRDN(BCStyle.O, "publet")
    builder.addRDN(BCStyle.CN, hostname)

    val notBefore = new util.Date(System.currentTimeMillis() - oneDay)
    val notAfter = new util.Date(System.currentTimeMillis() + (10 * oneYear))
    val serial = BigInteger.valueOf(System.currentTimeMillis())

    val certGen = new JcaX509v3CertificateBuilder(builder.build(),
      serial, notBefore, notAfter, builder.build(), pair.getPublic)
    val sigGen = new JcaContentSignerBuilder("SHA256WithRSAEncryption")
      .setProvider(BC).build(pair.getPrivate)
    val cert = new JcaX509CertificateConverter().setProvider(BC)
      .getCertificate(certGen.build(sigGen))
    cert.checkValidity(new util.Date())
    cert.verify(cert.getPublicKey)

    // Save to keystore
    val store = KeyStore.getInstance("JKS")
    if (keystore.exists()) {
      val fis = new FileInputStream(keystore)
      store.load(fis, keystorePassword.toCharArray)
      fis.close()
    } else {
      store.load(null)
    }
    store.setKeyEntry(hostname, pair.getPrivate, keystorePassword.toCharArray, Array( cert ))
    val fos = new FileOutputStream(keystore)
    store.store(fos, keystorePassword.toCharArray)
    fos.close()
  }
}
