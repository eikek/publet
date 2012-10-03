package org.eknet.publet.vfs.util

import java.text.NumberFormat

/**
 * Utility enum for dealing with sizes.
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 24.06.12 16:27
 */
object ByteSize extends Enumeration {

  private val cKibi = 1024
  private val cMegabi = cKibi * 1024
  private val cGigabi = cMegabi * 1024

  def fromString(str: String): ByteSize.SizeVal = str.toLowerCase match {
    case "bytes" => ByteSize.bytes
    case "kib" => ByteSize.kib
    case "mib" => ByteSize.mib
    case "gib" => ByteSize.gib
    case c@_ => sys.error("Unknown ByteSize constant: "+ c)
  }

  abstract class SizeVal(name: String) extends Val(name) {
    def toBytes(size: Double): Long

    def toKibi(size: Double): Double

    def toMegabi(size: Double): Double

    def toGigabi(size: Double): Double

    def normalize(size: Double): (Double, SizeVal)

    def normalizeString(size: Double): String = {
      val n = normalize(size)
      val nf = NumberFormat.getInstance()
      nf.setMaximumFractionDigits(2)
      nf.format(n._1) + " " + n._2.toString()
    }
  }

  val bytes: SizeVal = new SizeVal("Bytes") {
    def toBytes(size: Double) = size.toLong

    def toKibi(size: Double) = size / cKibi

    def toMegabi(size: Double) = size / cMegabi

    def toGigabi(size: Double) = size / cGigabi

    def normalize(size: Double): (Double, SizeVal) = {
      if (size > cKibi) kib.normalize(toKibi(size))
      else (size, this)
    }
  }

  val kib: SizeVal = new SizeVal("KiB") {
    def toBytes(size: Double) = (size * cKibi).toLong

    def toKibi(size: Double) = size

    def toMegabi(size: Double) = size / cKibi

    def toGigabi(size: Double) = size / cMegabi

    def normalize(size: Double): (Double, SizeVal) = {
      if (size > 1 && size < cKibi) (size, this)
      else {
        if (size > cKibi) mib.normalize(toMegabi(size))
        else bytes.normalize(toBytes(size))
      }
    }
  }

  val mib: SizeVal = new SizeVal("MiB") {
    def toBytes(size: Double) = (size * cMegabi).toLong

    def toKibi(size: Double) = (size * cKibi)

    def toMegabi(size: Double) = size

    def toGigabi(size: Double) = size / cKibi

    def normalize(size: Double): (Double, SizeVal) = {
      if (size > 1 && size < cKibi) (size, this)
      else {
        if (size > cKibi) gib.normalize(toGigabi(size))
        else kib.normalize(toKibi(size))
      }
    }
  }

  val gib: SizeVal = new SizeVal("GiB") {
    def toKibi(size: Double) = size * cMegabi

    def toMegabi(size: Double) = size * cKibi

    def toGigabi(size: Double) = size

    def toBytes(size: Double) = (size * cGigabi).toLong

    def normalize(size: Double): (Double, SizeVal) = {
      if (size > 1) (size, this)
      else {
        mib.normalize(toMegabi(size))
      }
    }
  }

}
