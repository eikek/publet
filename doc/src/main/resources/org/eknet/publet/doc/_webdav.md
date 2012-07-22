# Webdav Access

Resources can also be published via WebDAV. You need to configure the URL in
`settings.properties`. The URL and all sub-paths are then handled by a WebDAV
filter instead of the normal publet filter.

Resources below those configured paths are only available via webdav. Since
they must be mounted in the content tree, server templates can still access
them.

The list must use subsequent numbers, once no filter for a number exists, the
lookup process stops.

### Examples:

    webdav.filter.0=/dav/wikis/jdoe
    webdav.filter.1=/maven2
    webdav.filter.2=/dav/files