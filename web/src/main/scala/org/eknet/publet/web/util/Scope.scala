package org.eknet.publet.web.util

abstract sealed class Scope(name: Symbol)

object Request extends Scope('request)
object Session extends Scope('session)
object Context extends Scope('context)


