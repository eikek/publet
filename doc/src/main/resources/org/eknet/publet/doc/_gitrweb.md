# The _gitr-web_ module

This module provides a git repository manager for publet. Since the content root is also
a git repository, this module provides a means to browse the complete history of the web
page content.

It is strongly inspired by github, gitweb and gitblit. It has the following features:

* create new git repositories: you can create repositories that are either "root repositories"
  or located below a username. For example, a root repository is available at
  `http://mydomain.com/git/repo.git`, while normally you'd create a repository under your
  login `http://mydomain.com/git/login/repo.git`.
* you can have open and closed repositories. Open repository can be cloned by anyone. There is no
  need to authenticate.
* add/remove collaborators. This allows you to grant other user _pull_ or _push_ rights to your
  repository.
* repository web view:
    * browse repository contents
    * commit history (git log)
    * commit diff details

For repository creation, you need to grant the `gitcreate` permission. This allows the user to
create new git repositories under his/her login name. If you additionally grant the `gitcreateRoot`
permission, the user is also allowed to create root repositories.

The permission `gitadmin` allows to administrate the repository (add/remove collaborators, change
open/closed state etc) including to delete it! The owner of the repository automatically has this
permission.

All repositories are available under the `/git/` path. Thus, if publet is at `http://mydomain.com`
a repository of name "publet.git" can be cloned at `http://mydomain.com/git/publet.git`. The gitr-web
module is usually mounted at `/gitr` path (note the additional _r_!). The URL to the web view of
an repository is created by specifying an URL parameter of name `r`. For example, the Url
`http://mydomain.com/gitr/?r=publet.git` would point to the web view of the repository `publet.git`.
