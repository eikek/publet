# Git

The location of the git repositories is mapped to the path `git/`. The content
repository is located at `/git/contentroot.git`. If publet is installed at
`http://localhost:8080/publet` you can check out the repository using

    git clone http://localhost:8080/publet/git/contentroot.git

To have the changes reflected immediately you need to publish them to the
`master` branch.

If not specified otherwise in `permissions.xml`, every git repository is
_open_, which means anyone can clone it. Write access, on the other hand,
is protected by default. Only authenticated users can push to a repository.

If a repository is _open_, you can grant users write access by associating the
permission `push` with one of their groups. If a repository is closed, you
need to grant users explicite `pull` permission if they should be able to
access the repository. Please check the _Security_ section for some examples
and more explanation.