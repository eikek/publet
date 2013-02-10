## Counter Extension

The counter extension is tracking some data for each request, namely the
number of visits of an URL and the last time it was accessed.

To avoid counting requests too often, the IP address is cached in memory (not
on disk!) for a specific amount of time (default is 2h). If the same IP hits
the same URL within this time frame, it is not counted. The data is saved
inside a graph database on the server.

The data is available via the `CounterService`. Retrieve it via
`CounterExtension.service`.

You can specify a list of ip addresses in `settings.properties` that should
be discarded from counting (maybe you don't want to count your own accesses).

The key must start with `ext.counter.blacklist.` and appended with an ip address
or a valid hostname. A value of `true` will black-list the ip, a value of `false`
yields in counting accesses from this ip. If a hostname is given, it is re-resolved
in certain intervals to its ip address. The default interval is 15 hours, and it
can be overriden in `settings.properties` file:

    ext.counter.blacklistResolveInterval=1

The number is interpreted as _hour_. With the setting above, the hostnames are
re-resolved every hour. If the value is non-negative, the re-resolving is never
done. Invalid values will fallback to the default.

#### Examples:

    ext.counter.blacklist.110.110.110.110=true
    ext.counter.blacklist.127.0.0.1=true

By default, accesses from all ips are taken into account but not user agents
with words _bot_ or _spider_ in their name.

Additionally a list glob pattern can be specified that is matched against the uri.
The list can be defined to be a blacklist or whitelist. If it is a blacklist, only
uris that does not match a pattern in the list are counted. In case of a whitelist,
only those uris that match a pattern in the list are counted. Specify the pattern
in `settings.properties` with key `ext.counter.url.list`:

    ext.counter.url.list=/main/**, /publet/doc/**

This would count only URLs starting with `/main/`, all others are discarded, because
by default the list is a white list. You can add another property to make this a
blacklist:

    ext.counter.url.list.blacklist=true

The patterns are separated by comma. The glob can contain a `*` to match any sequence
of characters but not a `/`, a `?` to match any single character and `**` to match any
sequence of characters including `/`.
