package org.eknet.publet.impl

import org.eknet.publet.source.SourceRegistry
import java.net.URI
import org.eknet.publet.{Data, Publet}
import org.eknet.publet.engine.{PubletEngine, EngineRegistry}
import org.eknet.publet.impl.Conversions._

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 28.03.12 22:43
 */
protected[publet] class PubletImpl extends Publet with EngineRegistry with SourceRegistry {

  
  def process(uri: URI): Either[Exception, Option[Data]] = {
    Predef.ensuring(uri != null, "null is illegal")

    // lookup the source
    getSource(uri.schemeSymbol).lookup(uri) match {
      case None => Right(None)
      case Some(data) => {
        //lookup the engine according to the uri scheme
        engineFor(uri) match {
          case Left(x) => Left(x)
          case Right(engine) => engine.process(data)
        }
      }
    }
  }

  /**
   * Finds the engine to use for the given uri.
   *
   * @param uri
   * @return
   */
  private def engineFor(uri: URI): Either[Exception, PubletEngine] = {
    registeredEngines.find(_._1.pattern.matcher(uri.getPath).matches()) match {
      case None => Left(new RuntimeException("No engine found for: "+ uri))
      case Some(e) => Right(e._2)
    }    
  }

}


