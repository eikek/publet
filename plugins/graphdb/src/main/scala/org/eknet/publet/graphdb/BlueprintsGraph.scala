package org.eknet.publet.graphdb

import com.tinkerpop.blueprints.{KeyIndexableGraph, TransactionalGraph, Graph}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 21.05.13 18:53
 */
trait BlueprintsGraph extends Graph with TransactionalGraph with KeyIndexableGraph {

}
