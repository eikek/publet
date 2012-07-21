# Git

The location of the git repositories is mapped to the path `git/`. The content
repository is located at `/git/contentroot.git`. If publet is installed at
`http://localhost:8080/publet` you can check out the repository using

    git clone http://localhost:8080/publet/git/contentroot.git

To have the changes reflected immediately you need to publish them to the
`master` branch.

Publet also provides an extension called "gitr-web" that allows to manage
other git repositories. Those are then accessible at the same path.