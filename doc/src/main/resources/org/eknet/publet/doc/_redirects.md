# Redirects

You may have noticed that a request to the root `index.html` is immediately
redirected to `main/index.html`. This is a built-in redirect but you can add
other redirects by declaring them in the `settings.properties` file.

Redirect settings are declaring certain urls to be redirected immediately to
another URL. The keys must start with `redirect.` and name the URL to be
redirected. The value is the target URL.

#### Examples:

    redirect./welcome.html=/main/stuff/welcome.html
    redirect./main/welcome.html=/main/stuff/welcome.html

The first line declares the request to `/welcome.html` to be redirected to
`/main/stuff/welcome.html`. The second line makes a request to
`/main/welcome.html` to be redirected to the same target.