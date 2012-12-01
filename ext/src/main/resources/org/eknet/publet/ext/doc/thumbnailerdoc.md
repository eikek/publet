## Thumbnails

The `org.eknet.publet.ext.thumb.ThumbnailExtension` adds a convenient thumbnail feature. First, it
provides `Thumbnailer` service, that can be used in code to create a thumbnail of some image resource.
It will cache the thumbnail in a temporary directory.

A script mounted at `/publet/ext/thumbnail/thumb.png` looks for three URL parameters:

* `resource`: the path to a image resource for which a thumnail is requested (mandatory)
* `maxh`: the maximum height of the thumbnail (optional)
* `maxw`: the maximum width of the thumbnail (optional)

If `maxw` and `maxh` are not specified, some default value is used. If no such URL parameters
are present, the script will search the request and session for the following attributes:

* `thumbnail.maxHeight` of type `Int`
* `thumbnail.maxWidth` of type `Int`
* `thumbnail.imageResource` of type `ContentResource` (which is prefered) or `thumbnail.resourcePath` of type `String`

The thumbnail is only created, if the image is bigger than the requested size.

### Intercepting Image URLs

The extension will intercept URLs to an image resource if the URL parameter `thumb` is present. In
this case the image resource is looked up and put into the request atttribute map using the
`thumbnail.imageResource` key and the request is forwarded to the thumbnail script. Thus, a thumbnail
can be requested by just appending `?thumb` to the URL of any image. Optionally append `maxh` or `maxw`
with a corresponding value to configure the thumbnail size.

#### Examples

 * `http://my.server.com/main/images/dogs.png` -> the plain image in original size
 * `http://my.server.com/main/images/dogs.png?thumb` -> a thumbnail image of the default configured size
 * `http://my.server.com/main/images/dogs.png?thumb&maxh=400&maxw=600` -> a thumbnail image with at most
   600 pixel width and at most 400 pixel height

### Configuration

The thumbnails are saved to a directory on the server. The maximum entries or the maximum used disk space
can be configured in the `publet.properties` configuration file. For example:

    thumbnail.maxEntries=1000
    thumbnail.maxDiskSize=20MiB

The disk size can be specified as a plain number, in which case the unit `Byte` is assumed. Otherwise, one
of the following units can be used: `Bytes`, `KiB`, `MiB`, and `GiB`.

Only one of the two constraints can be specified! If both are given (as in the example), the `maxDiskSize`
value is prefered. If nothing is specified, a default value of `50MiB` is used.
