# Security

Publet implements a simple security strategy using [Apache Shiro](http://shiro.apache.org).

Resources are always inside a git repository. A repository can be _open_ or _closed_. An
open repository can be cloned by anybody, while closed repositories need explicit _pull_
permission for cloning and any other read access. Write access will always be protected by
checking an explicit _push_ permission.

Furthermore, restriction on resources can be defined.

These rules are specified in one xml file on a specific location

    /.allIncludes/config/permissions.xml

If this file is not present, a realm with one user called _superadmin_ is created. The
default password of this user is also _superadmin_, but can be specified in the
configuration file with property "superadminPassword". The _superadmin_ always has
all privileges.

Here is an example `permissions.xml` file explained:

    <publetAuth>
      <!-- user database
       -
       - users are filtered on login property. This must be unique
       - any duplicates are ignored, the first entry wins.
      -->
      <users>
        <user login="jdoe" password="098f6bcd4621d373cade4e832627b4f6" algorithm="md5">
          <fullName>John Doe</fullName>
          <email>jdoe@mail.com</email>
          <group>wikiuser</group>
          <group>editor</group>
        </user>
      </users>

      <!-- Defines repository states, default is `closed` -->
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
other resource. But modifications are not reflected until a manual
`reload()` is invoked on the authentication manager.

## User Database

The `<users/>` tag represents the user database. Each user is specified with
login and password. The password can be plain text or encrypted using one of
JDK's supported algorithms. In the example file, I created a md5 encrypted
password using the command

    echo -n "test" | md5sum

The `<group/>` tag specified the roles or groups the user belongs to. Permissions
can only be associated with groups.

## Repositories

The only repository seen so far is `contentroot`. But there may exists more
repositories. The tag `<repositories/>` defines the state for each. Any
repository not listed is always an _open_ repository. In the example the
`contentroot` is defined to be a closed repository. Access to a closed repository
will always be checked. That means that no anonymous user could now browse
a page anymore.

Additionally (and optional), an owner can be specified. This should be a
login of an registered user.

## Resource constraints

By specifying `<resourceConstraints/>` definitions, access to single resources can
be defined. An uri pattern is associated to a permission string. Each request
uri is matched against the list of uri patterns. If one matches (the first wins,
so order matters), the associated permission is checked against the current
subject.

The line

    <pattern name="/main/.allIncludes/config/*" perm="configure"/>

restricts access to all resources below `/main/.allIncludes/config/` to users
with the permission `configure`.

If you want to open resources, use the special permission string `anon`. If
this permission is found, the request handling resumes without access checks.

The line

    <pattern name="/main/*" perm="anon"/>

would allow access to any resource below `/main` to anybody.

## Permissions

Permissions are simple strings and associated to a group. You sould read
the [shiro documentation about wildcard permissions](http://shiro.apache.org/permissions.html).

There exist two predefined permissions: `push` and `pull`. They are usually
restricted to a specific repository, which is specified using the `<on/>`
tag. Internally the wildcard permission `pull:<repositoryname>` is created.

You can define any permission you like using the `<grant/>` tag and check them
anywhere in a template or script using the `Security` scala singleton:

    Security.checkPerm(perm:String)
    Security.hasPerm(perm: String): Boolean

See the source code of the `Security` object for more information.
