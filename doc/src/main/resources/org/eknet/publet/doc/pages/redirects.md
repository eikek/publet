## Redirects and Forwards

### Redirects

You may have noticed that a request to the root `index.html` is immediately
redirected to `main/index.html`. This is a built-in redirect but you can add
other redirects by declaring them in the `settings.properties` file.

Redirect settings are declaring certain urls to be redirected immediately to
another URL. The keys must start with `redirect.` and name the URL to be
redirected. The value is the target URL.

##### Examples:

    redirect./welcome.html=/main/stuff/welcome.html
    redirect./main/welcome.html=/main/stuff/welcome.html

The first line declares the request to `/welcome.html` to be redirected to
`/main/stuff/welcome.html`. The second line makes a request to
`/main/welcome.html` to be redirected to the same target.

There are the following redirects configured by default:

* `/` -> `/main/`
* `/index.html` -> `/main/index.html`
* `/index.htm` -> `/main/index.html`
* `/favicon.ico` -> `/main/favicon.ico`
* `/favicon.png` -> `/main/favicon.png`

### Forwards

Forwarding the request to some other resource can be achieved the same way
like redirects. Specify the resources to forward from in the `settings.properties`
file and use the prefix `forward.` (instead of `redirect.`). The value of such
a property specifies the resource to forward to.

##### Example:

    forward./robots.txt=/main/robots.txt

The example specifies to forward requests to the resource `robots.txt` at the
context root to the resource `/main/robots.txt`. This setting is already
configured as the only default forward.
