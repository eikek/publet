package org.eknet.publet.engine.scala

import org.eknet.publet.vfs.{ContentResource, Content}


/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 27.04.12 19:41
 */
trait PubletCompiler {

  /**
   * Compiles and executes the given scala script.
   *
   * The compiler is configured with the current classpath of
   * the application joint with the class path of the root
   * project and the project the given resource belongs to.
   * This is the closest parent project folder that is found
   * starting from the path at the given resource.
   *
   *
   * @param resource
   * @return
   */
  def evaluate(resource: ContentResource): Option[ScalaScript]

}
