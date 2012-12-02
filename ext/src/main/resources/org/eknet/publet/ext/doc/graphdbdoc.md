## Blueprints / OrientDb

The default database used by publet is a graph database. The database
[OrientDB](http://orientdb.org/) is included in the distribution while
code should be written against the [Blueprints](http://blueprints.tinkerpop.com/)
API that is part of the [Tinkerpop stack](http://tinkerpop.com/).
[Blueprints](http://blueprints.tinkerpop.com/) provides an API, that makes
your code independent from the underlying graph database.


### API / Usage

The service `GraphDbProvider` is provided to access databases using its
`getDatabase(name: String)` method. Each database is uniquely named and
the same database object is returned on subsequent invocations.

A listener is registered that will shutdown all databases on application
exit.