# Conventions

A set of conventions are used by a few features provided by publet. The
conventions are regarding the location of special files that publet scans for
in some cases.


## Special Directories

The structuring of the web contents is completely up to the user, of course.
However, there are the following directories that publet searches for
resources if needed by certain features.

* `.allIncludes/` only at the root of the repository
* `.includes/` at any level in the content tree

The `.allIncludes` directory exists only once at the root of the repository.
It contains things that are global to the whole application.

The `.includes/` directory, on the other hand, may appear in any directory in
the content tree. The contents of this directory "apply" only to the sibling
resources and to all resources below, that do not have their own `.includes`.
What "apply" is determined by the feature in question.

The default layout, for example, scans thoses directories for `.css` (and
other) files and will include them in each page. Thus "apply" means here, that
a `.css` file found in the `./includes` directory is only included in pages
located in the same directory as the `./includes` folder or below (unless a
new `includes` folder is found). A `.css` file in the `.allIncludes` folder is
included in _every_ page, regardless of its location.

In general, when looking for exactly one file, files in the nearest
`.includes` folder are always preferred and the `.allIncludes` directory is
used as fallback. If a set of files is looked up (like the `.css` files),
files from the nearest `.includes` directory and files found `.allIncludes`
are combined.


## Hidden files

With the hidden files convention you can create files that will not be served.
Any request to them will always result in a http 404 error. The idea is to
include them or call them from Scala scripts only.

A hidden file is any file that contains a segment starting with an underscore
`_` character. For example, the file `_userdoc.md` is hidden. A directory
starting with `_` would hide any files below it.

Some examples clarify this:

* `/_incl/hello.md` is hidden, because its parent directory name starts with `_`
* `/my_page.textile` is not hidden. There is an underscore character but not at the beginning
* `/dir1/_dir2/dir3/test.md` is hidden, because the name of the parent directory `_dir2` starts with an `_`
