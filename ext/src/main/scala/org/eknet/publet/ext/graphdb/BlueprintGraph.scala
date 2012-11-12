package org.eknet.publet.ext.graphdb

import com.tinkerpop.blueprints.TransactionalGraph
import com.tinkerpop.blueprints.IndexableGraph
import com.tinkerpop.blueprints.KeyIndexableGraph

/**
 * Super trait of features implemented by
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 23.06.12 17:00
 */
trait BlueprintGraph extends TransactionalGraph with KeyIndexableGraph