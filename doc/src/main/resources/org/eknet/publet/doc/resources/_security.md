# Security

Publet implements a simple security strategy using [Apache Shiro](http://shiro.apache.org).

Resources may be maintained in a git repository. A repository can be _open_ or _closed_. An
open repository can be cloned by anybody, while closed repositories need explicit _pull_
permission for cloning and any other read access. Write access will always be protected by
checking an explicit _push_ permission.

Furthermore, restriction on resources can be defined by URL patterns. These
permissions are checked on each request to the resource. Obviously it wouldn't
make sense to restrict resources in an open repository, as it can be cloned by
anybody. Thus, to make use of URL restrictions, use a closed repositories.

These rules are specified in one xml file on a specific location

    /.allIncludes/config/permissions.xml

If this file is not present, a realm with one user called _superadmin_ is created. The
default password of this user is also _superadmin_, but can be specified in the
configuration file with property "superadminPassword". The _superadmin_ always has
all privileges. As soon as the file `permissions.xml` is found, the default _superadmin_
account is disabled.

## Permission file explained

Here is an example `permissions.xml` file explained:

    <publetAuth>
      <!-- user database
       -
       - users are filtered on login property. This must be unique
       - any duplicates are ignored, the first entry wins.
      -->
      <users>
        <user login="jdoe" password="098f6bcd4621d373cade4e832627b4f6" algorithm="md5" digest="abcdef">
          <fullName>John Doe</fullName>
          <email>jdoe@mail.com</email>
          <group>wikiuser</group>
          <group>editor</group>
        </user>
      </users>

      <!-- Defines repository states, default is `open` -->
      <repositories>
        <repository name="jdoe/dotfiles" tag="closed" owner="jdoe"/>
        <repository name="wikis/mywiki" tag="open" owner="jdoe"/>
        <repository name="contentroot" tag="closed"/>
      </repositories>

      <!-- Defines permissions and associates them to roles -->
      <permissions>
        <!-- allows many on and to tags -->
        <grant name="PULL">
          <on>contentroot</on>
          <on>wikis/mywiki</on>
          <to>manager</to>
          <to>editor</to>
          <to>coders</to>
        </grant>
      </permissions>

      <resourceConstraints>
        <pattern name="/c/**" perm="anon"/>
        <pattern name="/sec/**" perm="superperm"/>
      </resourceConstraints>
    </publetAuth>

Note, the file is inside the repository, so you can edit it like any
other resource.

### User Database

The `<users/>` tag represents the user database. Each user is specified with
login and password. The password can be plain text or encrypted using one of
JDK's supported algorithms. In the example file, I created a md5 encrypted
password using the command

    echo -n "test" | md5sum

The `<group/>` tag specifies the roles or groups the user belongs to. Permissions
can only be associated with groups.

The `digest` property specifies the so called `HA1` value for authenticating
using the [Digest](http://en.wikipedia.org/wiki/Digest_access_authentication)
method. Currently, this is only used for authenticating WebDAV requests and is therefore
optional (if it is not present authentication does not work for WebDAV
requests). Please refer to the WebDAV documentation for more information.

### Repositories

The only repository seen so far is `contentroot`. But there may exists more
repositories. The tag `<repositories/>` defines the state for each. Any
repository not listed is always an _open_ repository. In the example the
`contentroot` is defined to be a closed repository. Access to a closed repository
will always be checked. That means that no anonymous user could now browse to
a page in this repository anymore.

Additionally (and optional), an owner can be specified. This should be a
login of an registered user.

### Resource constraints

By specifying `<resourceConstraints/>` definitions, access to single resources can
be defined. An uri pattern is associated to a permission string. Each request
uri is matched against the list of uri patterns. If one matches (the first wins,
so order matters), the associated permission is checked against the current
subject.

The line

    <pattern name="/main/.allIncludes/config/**" perm="configure"/>

restricts access to all resources below `/main/.allIncludes/config/` to users
with the permission `configure`.

If you want to open resources, use the special permission string `anon`. If
this permission is found, the request handling resumes without access checks.

The line

    <pattern name="/main/**" perm="anon"/>

would allow access to any resource below `/main` to anybody. The pattern can
contain the following wild cards:

* a star `*` to express many characters up to the next path delimiter `/`
* a question mark `?` to express exactly one character
* a double star `**` to express many characters crossing path boundaries

### Permissions

Permissions are simple strings and associated to a group. You sould read the
[shiro documentation about wildcard
permissions](http://shiro.apache.org/permissions.html).

There exist two predefined permissions: `push` and `pull`. They are usually
restricted to a specific repository, which is specified using the `<on/>` tag.
Internally the wildcard permission `pull:<repositoryname>` is created.

You can define any permission you like using the `<grant/>` tag and check them
anywhere in a template or script using the `Security` scala singleton:

    Security.checkPerm(perm:String)
    Security.hasPerm(perm: String): Boolean

See the source code of the `Security` object for more information.


## Authentication

There is standard login functionality provided with publet. It uses a simple
login template and a script that executes the login. If a unauthorized error
occurs somewhere in the application, the client is redirected to this login
template.

The path to the default login template is: `/publet/templates/login.jade`. So
you can login at this url: `/publet/templates/login.html`.

The template looks for a url parameter of name `redirect` and will replace
the URL of the browser with that value after successful login. So you could
write the request `/publet/template/login.html?redirect=/main/index.html` to
be redirected to the main screen after successful login.

A more useful variation is to use the current URL for redirect. This can be
achieved easily with some scala code in your template (jade):

    - def urlOf(str: String) = PubletWebContext.urlOf(str)
    - val loginUrl = urlOf("/publet/templates/login.html") + "?redirect="+ urlOf(PubletWebContext.applicationUri)
    - val logoutUrl = urlOf("/publet/scripts/logout.json?redirect=" + urlOf("/"))
    ul
      - if (Security.isAuthenticated)
        li
          a(href={ logoutUrl })
            i.icon-hand-right
            | Logout
      - if (!Security.isAuthenticated)
        li
          a.btn.btn-inverse(href={ loginUrl })
            i.icon-user.icon-white
            | Login

The example above renders a login link, if the current user is anonmyous. It
will render a logout link, if the current user is logged in. As you can see,
the logout is performed using the `logout.json` script.

The scala script that the login form is sending its content to, is at the path
`/publet/scripts/login.json`. It will use the information provided by the
login form and issue an login using [Apache Shiro's](http://shiro.apache.org)
API.

The code is very simple (thanks to shiro):

    object Login extends ScalaScript {

      def serve() = {
        val username = PubletWebContext.param("username")
        val password = PubletWebContext.param("password")
        val rememberMe = PubletWebContext.param("rememberMe")
        if (username.isDefined && password.isDefined) {
          val subject = Security.subject
          val token = new UsernamePasswordToken(username.get, password.get.toCharArray)
          token.setRememberMe(checkboxToBoolean(rememberMe))
          try {
            subject.login(token)
            makeJson(Map("success"->true, "message"->"Login successful."))
          } catch {
            case e:ShiroException => {
              makeJson(Map("success"->false, "message"->"Login failed."))
            }
          }
        } else {
          makeJson(Map("success"->false, "message"->"No login information given."))
        }
      }

      def checkboxToBoolean(str: Option[String]): Boolean = {
        str.exists(_ == "on")
      }
    }

At first the parameters are extracted from the request using
`PubletWebContext.param("name")`. Then a authentication token is created and
passed to the `login()` method of shiro's `Subject`. That's all about it.


### Customize Login

You can create a custom login template that either reuses the provided
`login.json` script or using a custom one. Please have a look at the [source
of the provided template](../templates/login.jade) for a starting point.

To have publet redirect to your custom template on unauthentication errors,
add its location to the `settings.properties` file:

    publet.loginUrl=/path/to/your/login-template.jade

If you like to execute your own code for login, just add a new template that
uses your custom script.