# clj-holmes

[![Clojars Project](https://img.shields.io/clojars/v/org.clojars.clj-holmes/clj-holmes.svg)](https://clojars.org/org.clojars.clj-holmes/clj-holmes)

A CLI SAST (Static application security testing) tool which was built with the intent of finding vulnerable Clojure code via rules that use a simple [pattern language](https://github.com/clj-holmes/shape-shifter). Although finding vulnerabilities is its main purpose, `clj-holmes` can also be used to find any kind of code pattern.

# Installation
Download the release for your OS (mac or linux), copy it to a directory in your `$PATH` and add executable permission to the binary.

## Linux example
```
curl -L https://github.com/clj-holmes/clj-holmes/releases/latest/download/clj-holmes -o /tmp/clj-holmes
sudo install -m 755 /tmp/clj-holmes /usr/local/bin/clj-holmes
rm /tmp/clj-holmes
```

# Rules
All public rules can be found [here](https://github.com/clj-holmes/clj-holmes-rules). It is also possible to maintain your own set of rules.

`clj-holmes` currently supports the following rules sources:

## GitHub
The [GitHub](https://github.com/clj-holmes/clj-holmes/blob/main/src/clj_holmes/rules/wagon/github.clj) wagon supports public and private repositories. In order to fetch rules from a private repository the environment `GITHUB_TOKEN` variable needs to be set.

To fetch a rule set `clj-holmes` expects a GitHub repository URL following the specification below:

`git://username/project-name#branch-name`

## Fetching Rules
```
NAME:
 clj-holmes fetch-rules - Fetch rules from an external server

USAGE:
 clj-holmes fetch-rules [command options] [arguments...]

OPTIONS:
   -r, --repository S        git://clj-holmes/clj-holmes-rules#main  Repository to download rules
   -o, --output-directory S  /tmp/clj-holmes-rules/                  Directory to save rules
   -?, --help
```

In order to execute a scan it is necessary to fetch the rules first. This can be achieve with the following command.

`clj-holmes fetch-rules`

It's also possible to provide another source for a rule set by adding the `-r` or `--repository` parameter followed by the GitHub repository URL.

`clj-holmes fetch-rules -r git://clj-holmes/clj-holmes-private-rules#main`

# Scanning a Project
```
NAME:
 clj-holmes scan - Performs a scan for a path

USAGE:
 clj-holmes scan [command options] [arguments...]

OPTIONS:
   -p, --scan-path S*                                           Path to scan
   -d, --rules-directory S         /tmp/clj-holmes-rules/       Directory to read rules
   -o, --output-file S             clj_holmes_scan_results.txt  Output file
   -t, --output-type sarif|stdout  stdout                       Output type
   -r, --rule-tags S                                            Only use rules with specified tags to perform the scan
   -i, --ignored-paths S                                        Glob for paths and files that shouldn't be scanned
   -?, --help
```

After fetching the rules, it is possible to execute a scan by providing the `-p` or `--scan-path` parameter followed by the path of the Clojure project to be scanned.

`clj-holmes scan -p /tmp/clojure-project`
