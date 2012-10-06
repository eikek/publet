# Partitions

Partitions are a concept in publet to make resources from multiple sources
available to the server. The idea is taken from unix/linux systems where you
can mount different file systems in one tree.

A partition in publet is some container holding resources. The very simple
case is a directory on the file system of the server that is running publet.
Another implementation is the `GitPartition` that maintains a git working copy
and a bare repository.

Those partitions are then "mounted into publet". The root container is the
root of all publet resources. Thus it makes up the structure of the content
and therefore the URLs to the resources.

You may have noticed, that all resources are always accessed behind the
`main/` path. For example, the root `index.html` is redirected to
`main/index.html`. That is because the git repository of the content root is
mapped to the path `main/`. This is necessary to allow other applications to
be added next to the main resources. In other words, publet instantiates a
`GitPartition` and mounts it to the path `main/`.

## Known partition types

There are few partition types implemented so far:

* file system
* _git tandem_
* classpath
* map

The file system type is just a plain directory. The "Git tandem" is the term
for a git bare repository and a working copy that is kept in sync with the
bare repository.

The file system partition is a good choice to store big files that don't need
to be tracked and versioned in a git repository.

Other container implementations include the `ClasspathContainer` and
`MapContainer`. These are useful when serving resources from within the
classpath, or fetched otherwise.


## Specifying partitions

On startup, publet scans the `settings.properties` file for partition
settings. The settings define what partitions are mounted into the content
tree. There are two types of partitions available:

1. `fs` = file system: This creates a plain folder on the
   server and hooks it into the content tree.
2. `git` = git repository: This creates a git repository
   and a working copy on the server. On each update a commit
   is issued to the bare repository. The repository is also
   available via http.

You need to specify the type, the directory (which is a relative
directory name) and a list of mount points. The mount points can
be separated by whitespace, comma or semicolon.

The `contentroot` git partition is always mounted (and created).
These settings only affect additional partitions.

The list must use subsequent numbers, as the lookup process
stops if a `partition.x.type` key does not exist for some x,
while x is incremented by one.

#### Examples:

    partition.0.type=fs
    partition.0.dir=parts/files
    partition.0.mounts=/files,/dav/files

    partition.1.type=git
    partition.1.dir=wiki/eike
    partition.1.mounts=/wikis/eike,/dav/wikis/eike

    partition.2.type=fs
    partition.2.dir=artifacts/maven2
    partition.2.mounts=/maven2
