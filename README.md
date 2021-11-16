# clj-holmes

[![Clojars Project](https://img.shields.io/clojars/v/org.clojars.clj-holmes/clj-holmes.svg)](https://clojars.org/org.clojars.clj-holmes/clj-holmes)

Clojure SAST (Static application security testing).

# Installlation
`sudo curl -L https://github.com/clj-holmes/clj-holmes/releases/latest/download/clj-holmes -o /usr/local/bin/clj-holmes`

# Rules
All public rules can be find in [here](https://github.com/clj-holmes/clj-holmes-rules). But it is possible to maintain your own set of rules.

clj-holmes currently supports the following rules source.

## Github
The [Github](https://github.com/clj-holmes/clj-holmes/blob/main/src/clj_holmes/rules/wagon/github.clj) wagon supports public and private repositories, If it is a private repository an environment variable `GITHUB_TOKEN` needs to be set.

In order to work properly Github repository url needs to follows the specification above to works on clj-holmes.

`git://username/project-name#branch-name`

## Fetch
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

In order to execute a scan it is necessary to fetch the rules first which can be achieve with the following command.

`clj-holmes fetch-rules`

It's possible to provide another rules sources adding the `-r` or `--repository` parameter and the repository.

`clj-holmes fetch-rules -r git://clj-holmes/clj-holmes-private-rules#main`

# Scan
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

After downloading the rules it's possible to execute a scan only by providing the `-p` or `--scan-path` parameter value which must point to a clojure project.

`clj-holmes scan -p /tmp/clojure-project`
