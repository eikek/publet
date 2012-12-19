## JMX Connector

For accessing mbeans, there is a connector started that exposes the platforms mbean server. It is only started,
if a `publet.jmx.serviceUrl` configuration value is defined. This url is used to bind the service to. For example:

    publet.jmx.serviceUrl=service:jmx:jmxmp://localhost:9012

Note, to use the `jmxmp` protocol, you must download the `jmxremote_optional.jar` from [Oracle](http://www.oracle.com/technetwork/java/javase/tech/download-jsp-141676.html)
(search for _Java Management Extensions (JMX) Remote API Reference Implementation_) and add it somehow to the
classpath. You can drop it in the `plugins` directory or add it as an extension to your java installation -- put
the jar in `$JRE/lib/ext` directory. This way it is globally available. Of course, all clients need this jar, too.

The connector is not protected by default. It can be protected by specifying `publet.jmx.protected=true` in
the config file. Then only users with permission `jmx:connector` are allowed to connect. Additionally, the
client must send the credentials as an Array where the first element is the username and the second the password.

Usually, protected jmx connectors are not necessary, since ssh tunneling can be used instead. For example on
linux, use ssh command to create  a tunnel from `localhost:9999` to `someserver:9910`:

    ssh user@someserver -L 9999:localhost:9910

Then connect via `localhost:9999`.

### Registering MBeans

Registering MBeans is done by simply creating a binding in the guice module. There is a binding listener
that will register an object that complies to the mbean convention to the platforms MBeanServer. If the
class implements an interface ending in `MBean` than this object is considered to be an MBean, whether
it complies to the naming convention or not. If it does not, it is wrapped in a `DynamicMBean` to be able
to register it.

If a provider or factory method is used, registering the mbean must be done manually:

    val service = ... //complies to MBean spec
    JmxService.registerMBean(service)
