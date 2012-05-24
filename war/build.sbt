seq(webSettings :_*)

libraryDependencies += "org.eclipse.jetty" % "jetty-webapp" % "8.0.1.v20110908" % "container"

port in container.Configuration := 8081

