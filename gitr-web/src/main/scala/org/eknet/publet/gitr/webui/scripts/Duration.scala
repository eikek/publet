package org.eknet.publet.gitr.webui.scripts

import java.util.concurrent.TimeUnit

/**
 * From `startMillis` until now.
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 29.05.12 10:03
 *
 */
object Duration {

  abstract sealed class Unit(val milliFactor: Long, val name: String) {
    require(milliFactor > 0, "milliFactor must not be negative")
    def toMillis(n: Long) = n * milliFactor
    def convert(n: Long, unit: Unit) = toMillis(n) / unit.milliFactor
    override def toString = name.toUpperCase
  }

  object Year extends Unit(365L * 24 * 60 * 60 * 1000, "year")
  object Month extends Unit(30L * 24 * 60 * 60 * 1000, "month")
  object Day extends Unit(24L * 60 * 60 * 1000, "day")
  object Hour extends Unit(60L * 60 * 1000, "hour")
  object Minute extends Unit(60L * 1000, "minute")
  object Second extends Unit(1000, "second")
  object Millis extends Unit(1, "millisecond")

  val units = List(Year, Month, Day, Hour, Minute, Second, Millis)

  final class NumberExtra(number: Long) {
    def millis = new NumberWithUnit(number, Millis)
    def seconds = new NumberWithUnit(number, Second)
    def second = seconds
    def minutes = new NumberWithUnit(number, Minute)
    def minute = minutes
    def hours = new NumberWithUnit(number, Hour)
    def hour = hours
    def days = new NumberWithUnit(number, Day)
    def day = days
    def months = new NumberWithUnit(number, Month)
    def month = months
    def years = new NumberWithUnit(number, Year)
    def year = years
  }
  final case class NumberWithUnit(number: Long, unit: Unit = Millis) {

    lazy val inMillis = unit.toMillis(number)
    def in(unit: Unit) = this.unit.convert(number, unit)

    private lazy val ageStream = units.toStream.map(u => u -> in(u))
    lazy val age = ageStream.find(t => t._2 > 0).getOrElse((unit, number))
    lazy val ageString = age._2 + " " + age._1.name+ (if (age._2 > 1) "s" else "")

    def until(nwu: NumberWithUnit) = NumberWithUnit(nwu.inMillis - this.inMillis, Millis)
    def untilNow = NumberWithUnit(System.currentTimeMillis() - this.inMillis)
  }

  implicit def intWithMillis(n: Int) = new NumberExtra(n)
  implicit def longWithMillis(n: Long) = new NumberExtra(n)
}
