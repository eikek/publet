# Webdav Access

Resources can also be published via WebDAV. You need to configure the paths in
`settings.properties`. The configured paths and all sub-paths are then handled
by a WebDAV filter instead of the normal publet filter.

Resources below those configured paths are only available via webdav. Since
they must be mounted in the content tree, server templates can still access
them (You can use them in templates, for example create download links to those
resources; but templates are not processed).

The list must use subsequent numbers, once no filter for a number exists, the
lookup process stops.

### Examples:

    webdav.filter.0=/dav/wikis/jdoe
    webdav.filter.1=/maven2
    webdav.filter.2=/dav/files

The realm name that is used for authentication is also specified in the
`settings.properties` file:

    webdav.realmName=WebDav Area

The default name is `WebDav Area`. It is currently not supported to specify
different realm names. This realm name is used for all resources.

You can disable/enable webdav support for the whole application in the
configuration file `publet.properties` by disabling the webdav extension:

    org.eknet.publet.webdav.WebdavModule=false

If this is set to `false`, all webdav related settings are ignored, since the
webdav filter is disabled. By default, webdav support is enabled.

## Authentication

For better client compatibility, the WebDAV filter supports authentication via
a methods called
[Digest](http://en.wikipedia.org/wiki/Digest_access_authentication) and
[Basic](http://en.wikipedia.org/wiki/Basic_access_authentication). Some
clients expect digest authentication and do not work without.

If the site does not use TLS/SSL, the Digest method is much preferred as the
password is not send in plain text to the server.

However, using the Digest authentication method, the user database must store
an additional property for each user. This property is called the `HA1` and is
the md5 checksum of the string `username ':' realm ':' plaintextPassword`. In
the `permissions.xml` file, this information is stored in the `digest`
property of the `<user/>` tag.

    <user name="jdoe" password="234234234" algorithm="md5" digest="abcdef">
     ...
    </user>

On Linux, you can create the `HA1` easily using the `md5sum` command:

    echo -n "jdoe:WebDav Area:secret" | md5sum
    b6c1d28b112aa44e79ea508939dd782c  -

You can specify different passwords for WebDAV and normal authentication, if
you like. Please make sure that you use the correct realm name. If you change
the realm name in `settings.properties` the `digest` value of all user entries
must be updated accordingly (which can be done by a simple password change as
described below; login on the web site is still working.).

You can also let publet create the digest for you by performing the _Change password_
action. The _ext_ extension that is distributed with publet provides a
template with this feature, that you can include in your own site. Simply
create a page like the following `mydata.page` with this content:

    ---
    title: My Data

    --- name:content pipeline:jade
    .page-header
      h1 My Data

    =include("/publet/ext/includes/templates/_myData.jade")

Then go to the new page `mydata.html` and change your password. Note that this
only works for authenticated users. This will update the user entry in the
`permissions.xml` file with the password and a correct digest value. Please
see the "Ext" section in the [Extension documentation](extensions/) for more
information on the _myData_ template.


## Client Compatibility

Some WebDAV clients expect a certain behaviour of the server to function
correctly. That means that some clients may work only partially or not at all.
This is most probably due to how publet exposes the WebDAV feature. To be
honest, I lowered my efforts, once <code>mvn deploy</code> and <code>sbt
publish</code> started working for me. The WebDAV feature is based on
[Milton](http://milton.io) - there you can find a nice [page on this
topic](http://milton.io/guide/m18/compat/index.html). 

The following clients seem to work:

* Dolphin (Filemanager in KDE)
* Windows XP (using "network folder")
* cadaver (Linux command line client)
* `mvn deploy` and `sbt publish` ;-)
