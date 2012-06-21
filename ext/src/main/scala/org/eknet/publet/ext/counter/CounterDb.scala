package org.eknet.publet.ext.counter

import com.tinkerpop.blueprints.impls.orient.OrientGraph
import org.eknet.publet.web.Config
import java.io.File
import com.tinkerpop.blueprints.TransactionalGraph.Conclusion

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 20.06.12 23:46
 */
object CounterDb {


  private lazy val dbUri = "local://"+new File(Config.configDirectory, "databases"+ File.separator +"extdb").getAbsolutePath
  lazy val db = new OrientGraph(dbUri)

  private val txthread = new ThreadLocal[Boolean]() {
    override def initialValue() = false
  }

  def withTx[A](f: OrientGraph=>A) = {
    if (!txthread.get()) {
      db.startTransaction()
      txthread.set(true)
      try {
        val r = f(db)
        db.stopTransaction(Conclusion.SUCCESS)
        r
      } catch {
        case e: Throwable => {
          db.stopTransaction(Conclusion.FAILURE)
          throw e
        }
      } finally {
        txthread.remove()
      }
    } else {
      f(db)
    }
  }

  lazy val referenceNode = {
    import collection.JavaConversions._
    val currentVersion = 0
    val referenceProperty = "__9b042b5a-bb0d-4656-a037-2b5b78fd543f"
    CounterDb.withTx { db =>
      db.getVertices(referenceProperty, 0).headOption match {
        case Some(v) => v
        case None => {
          val v = db.addVertex()
          v.setProperty(referenceProperty, 0)
          v.setProperty(versionProperty, currentVersion)
          v
        }
      }
    }
  }

  val versionProperty = "__counterDbVersion"
}
