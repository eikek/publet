package org.eknet.publet.impl

import org.eknet.publet.source.SourceRegistry
import org.eknet.publet.impl.Conversions._
import collection.mutable.ListBuffer
import org.eknet.publet.{Uri, ContentType, Data, Publet}
import org.eknet.publet.engine.{EngineResolver, EngineRegistry}

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 28.03.12 22:43
 */
protected[publet] class PubletImpl extends Publet with EngineResolver with EngineRegistry with SourceRegistry {


  def process(uri: Uri, target: ContentType) = process(uri.urisFor(target).head)

  def process(uri: Uri): Either[Exception, Option[Data]] = {
    Predef.ensuring(uri != null, "null is illegal")

    //lookup engine for uri pattern
    val engine = resolveEngine(uri).getOrElse(sys.error("No engine found for uri: "+ uri))
    
    // lookup the source
    findSourceFor(uri) match {
      case None => Right(None)
      //lookup the engine according to the uri scheme and process data
      case Some(data) => engine.process(data, uri.targetType.get)
    }
  }


  /**
   * Finds resources that matches the name of the specified uri
   * but not necessarily the file extension.
   * <p>
   * For example, finds a `title.md` if a `title.html` is requested,
   * while `title.html` will be the first one on the Seq if it exists.
   * </p>
   *
   * @param uri
   * @return
   */
  private def findSourceFor(uri: Uri): Option[Seq[Data]] = {
    val source = getSource(uri.schemeSymbol)
    val buffer = new ListBuffer[Data]

    // create a list of uris of all known extensions
    val urilist = uri.urisForTarget.toSeq ++
      ContentType.all.filter(_ != uri.targetType).flatMap( _.extensions.map(uri.withExtension(_)) )
    
    //lookup all uris and returns list of results
    urilist.foreach (source.lookup(_).flatten( buffer.+= ))
    if (buffer.isEmpty) None else Some(buffer.toSeq)
  }

}


