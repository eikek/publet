# Security

Publet implements a security strategy using [Apache Shiro](http://shiro.apache.org).

Publet is a web application that serves resources from a configured content tree. The content
tree is a collection of _partitions_. A partition is a special container that is said to "own"
(in a security sense) its child resources and can introduce its own security scheme. That is
necessary, since it might be accessed by other protocols that publet is not aware of.

The publet server, of course, does not need to honor partition access conditions. It could very
well decide to still serve this resource, but not the other. In particular, if a partition denies
access to the current subject, it is assumed that the subject is not able to access *any* resource
in that partition. That means, if the publet server decides to still deliver the resource, it can be
sure that this is the only way for the current subject. On the other hand, if a partition grants
access, then the current subject can access *all* resources in this partition in some other way. In
this case, it makes no sense to obey any other restrictions. This idea is the basis of publet's
`resource` permissions.

## Git Partition

Publet comes with one such concrete partition: the git partition. It is a container of resources
that is backed by a git repository. The git repository can also be accessed by the git command
line client (or any other git client, of course). The git servlet provided by JGit is taking care
of those requests.

The security scheme for git repositories is quite simple. A git permission is marked by the `git`
domain and can be granted to repositories. Repositories are identified by its names (without the .git
extension). The following actions are known to publet:

* push, pull -- means read and write access to a repository
* admin -- for editing repository attributes
* create, createRoot -- for creating new repositories, either below the own username only or at a place of choice

It is (currently) not possible to define finer grained privileges, like by branch.

The permissions are checked against a repository model, which is defined in some database. A
repository model provides the following information

* name -- the repository name, used for identifying
* tag -- can be _open_ or _closed_
* owner -- the username of the owner of this repository

The repository tag describes whether this is an open or closed repository. Open repositories can be
read by everyone, no matter if authenticated or not. Closed repositories always require either pull
or push permissions.

The repository owner is always granted all repository permissions implicitely (which are pull, push and admin).

## Resource Permissions

Resource permissions apply to the publet web server and they can be used to overrule the partition
security schemes as described above.

A resource permission is marked by the `resource` domain and can be granted for a specific set
of resources identified by its paths in the content tree. The resource set is defined conveniently
by using globs, `/main/priv/**` for example means "any resource below `/main/priv/`". The possible
actions are `read` and `write`, while `write` includes the deletion of existing and creation of
new resources.

Resource permissions can be used to widen the access scope implied by a partition.





When publet
is requested to serve a resource, it finds the partition the resource belongs to, and can asks
whether it is allowed to read/write the resource.




## Repositories

A git repository can have one of two states: _open_ or _closed_. For open repositories
read access will not be checked, but write access is always checked. For closed repositories
one needs explicit permission for reading and writing. That refers to the repository as a
whole -- it affects all resources in it. It is not possible to authorize git requests on branches
or other finer granularity. That implies, that any resource from an _open_ repository can be
accessed by everybody. The following actions are defined:

* `pull` -- means read access, checked only for closed repositories, when cloning or pulling
* `push` -- means write access, checked on push
* `admin` -- for editing open/closed state and other repository attributes

The next two are related to creating new repositories:

* `create` -- for creating a new personal git repository. The repository is created inside the namespace of the current user.
* `createRoot` -- for creating new git repositories in the root of the repository tree.

Furthermore, a git repository can have an owner. This owner always has pull, push and admin
privileges granted implicitely.

When accessing resources over the web, more granular access checks are possible. Restricting read access
makes obviously only sense for resources coming from closed git repositories or non git repositories. Usually, you
might want to have a closed repository, but open up certain paths to the public.

Permissions for the resource domain can be used to grant `read` and `write` privileges for a set of
resources. The set is described via a glob pattern - like `/aa/bb/**` or `/aa/*/*.pdf`. This allows
to grant certain users read/write privileges to a chosen part of a repository.

Finally, to open up a set of resources to the public, it is possible to define two sets of resource
sets: one for restricted and one for anonymous access. On each request (to a non-open repository),
the url is first matched against the defined set of restricted resource patterns. If no match is found,
the url is matched against the defined anonymous set of patterns. If a match is found, the request
is served, otherwise the read or write action is checked against the current subject.

Examples


1. allow sub paths
<repository name="contentroot" tag="closed"/>    - close the repository
<resources>
  <open>/main/public/**</open>                   - open up everything below /main/public/. access to other
</resources>                                       resources is checked by default.

2. explicit deny
<repository name="contentroot" tag="closed"/>    - close the repository
<resources>
  <open>/main/**</open>                          - open up everything below /main/, but
  <restrict>/main/members/**</restrict>          - restrict access to everything below /main/members/
</resources>
<grant>                                          - all "developers" are able to access /main/mebers/**
  <perm>resource:read:/main/members/**</perm>
  <to>developers</to>
</grant>

3. selective
<repository name="contentroot" tag="closed"/>    - close the repository
<resources>
  <open>/main/**</open>
  <restrict>/main/projectx/**</restrict>
  <restrict by="chefaction">                     - access to everything below /main/projectx/chefsonly
    /main/projectx/chefsonly/**                    is restricted by checking a resource permission using the
  </restrict>                                      given part "checkaction".
</resources>
<grant>                                          - all "developers" are able to access /main/projectx/**
  <perm>resource:read:/main/projectx/**</perm>
  <to>developers</to>
</grant>
<grant>                                          - all "managers" are also able to access /main/projectx/chefsonly/**
  <perm>resource:read:/main/projectx/**</perm>
  <perm>resource:chefaction:/main/projectx/chefsonly/**</perm>
  <to>managers</to>
</grant>

4. more selective
<repository name="contentroot" tag="closed"/>    - close the repository
<resources>
  <open>/main/**</open>
  <restrict>/main/projectx/**</restrict>
  <restrict by="chefaction" on="write">          - protect write access by checking "chefaction" againts the url
    /main/projectx/chefsonly/**                    read access is not affected by this rule
  </restrict>
</resources>
<grant>                                          - all "developers" are able to read and write /main/projectx/**
  <perm>resource:read,write:/main/projectx/**</perm>
  <to>developers</to>
</grant>
<grant>                                          - all "managers" are able to access /main/projectx/chefsonly/**
  <perm>resource:chefaction:/main/projectx/chefsonly/**</perm>
  <perm>resource:read,write:/main/projectx/**</perm>
  <to>managers</to>
</grant>


The order of <restrict> and <open> definitions does not matter. If a resource matches one of the resticted
patterns, the corresponding permission is checked. It is also checked if there is no match in the set of
open patterns. Basically on each access, the git permission is checked first (if applicable) and if that
returns a positive response, access is granted. Only if the "global" access to the git repository is denied,
the resource permissions are used to "overwrite" the restrictions.

## What happens

when a requests hits the server.

GET: /main/projectx/index.html

1. if (is git repo && (is open || check pull) SERVE else
2. rset = restriction patterns for read
   for r <- rset check("resource"+ action(r)+":"+url)
3. if (!rset.isEmpty) SERVE, else:
     aset = open patterns
     if (url in aset) SERVE else
4. check("resource:read:"+url)


PUT: /main/projectx/image.png

1. if (is git repo && has push) SERVE, else
2. rset = restriction patterns for write
   for r <- rset check("resource"+ action(r)+":"+url)
   if (!rset.isEmpty) SERVE, else
3. check("resource:write:"+url)

The first line on both examples refer to the git repository. It is always asked first, because
it controls all its contents. That means, if the parent of all resources grants access, that it
must be assumed that there may be another way to retrieve the content (which is true for git
repositories). Thus it makes no sense to have a greater restriction now.

## Permissions

A resource is located by its URL which is internally mapped to a path in
the content tree. A resource is by default access protected,
which means you need explicit rights for accessing it.

You can associate resource permissions to groups of the form

    resource:read,write,delete,create:/path/pattern/**,/path/*/pattern/**

The above defines the permissions to read, write, delete and create resources
that match the patterns /path/pattern/** or /path/*/pattern/**. A shorter version
would be

    resource:*:/path/pattern/**,/path/*/pattern/**

A permission that would grant all access actions to all resources looks likt this

    resource:*:/**

or

    resource:*:**

Note the double '*' characters. While the actions read, write, delete and create
are used by publet, you can also define other actions and check them.

You can furthermore define to skip whole authentication machinery for a url pattern:

    <pattern name="/public/**" perm="anon" />

With the above definition in place, requests to resources below /public/ are served
without authorization or authentication. The "anon" string is not a permission to
shiro, but it completely skips the authentication and authorization filters.

If the resource is coming from a git repository, things are a bit more complicated
since now the repository state and two other permissions come into play. There is
the `git:pull` and `git:push` permission which (in context of resource access) are
just groupings of the already known permissions. So `git:push` groups create, write
and delete while `git:pull` is the same as read. In context of a git repository every
user with `git:pull` permissions can clone a repository, so the user can access any
resource within it.

By default, a repository is _open_ if not otherwise defined. That means that every
user can clone the repository. More precise it defines to skip access checks for the
repository. It is compares therefore to the "anon" special permission for resources.

Since the repository is considered open, if undefined, the following possibilities
can occur when requesting a resource.


        repo      perm        | canRead?   canWrite?
        ----------------------|---------------------
        open      write|push  |  yes        yes
        closed    write|push  |  yes        yes
        open      read|pull   |  yes        no
        closed    read|pull   |  yes        no
        open       -          |  yes        no
        closed     -          |  no         no
        open       anon       |  yes        no
        closed     anon       |  yes        no


## Permission Strings

Permission strings are used as described in shiro's [manual](http://shiro.apache.org/permissions.html).
There exist predefined permission patterns:

    git:<git-action>:<reponame>
    resource:<resource-action>:<uri-pattern>

where the following actions exists:

    git-action = push, pull, createown, createroot, delete, edit
    resource-action = read, write, delete

The the implications exists:

* `git:pull:<repo>` -> `resource:read:<pattern>` if `pattern` points to a resource inside the git repository `repo`.
* `git:push:<repo>` -> `resource:write,delete,create:<patern>` if `pattern` points to a resource inside the git repository `repo`.

The permissions `createown` and `createroot` allow to create a git repository, either below its own user name or
as a root repository.


---

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
        <user login="jdoe">
          <fullName>John Doe</fullName>
          <email>jdoe@mail.com</email>
          <password>098f6bcd4621d373cade4e832627b4f6</password>
          <algorithm>md5</algorithm>
          <digest>abcdef</digest>
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

      <!-- Defines permissions and associates them to groups -->
      <permissions>
        <!-- allows many perm and to tags; information is collected from all grant tags -->
        <grant>
          <to>manager</to>
          <to>editor</to>
          <perm>git:pull:*</perm>
          <perm>resource:*:*</perm>
        </grant>
        <grant>
          <to>coders</to>
          <perm>resource:read:/**</perm>
        </grant>
        <grant>
          <to>anonymous</to>
          <perm>resource:read:/**</perm>
        </grant>
      </permissions>

      <resourceConstraints>
        <pattern name="/sec/**" perm="superperm"/>
        <pattern name="/**" perm="anon"/>
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