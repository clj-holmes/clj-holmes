(defproject clj-holmes "0.1.0"
  :plugins [[lein-ancient "0.6.15"]
            [lein-cljfmt "0.6.4"]
            [lein-nsorg "0.3.0"]
            [jonase/eastwood "0.3.10"]]

  :dependencies [[org.clojure/clojure "1.10.3"]
                 [org.clojure/tools.namespace "1.1.0"]
                 [org.clojure/data.json "2.4.0"]
                 [borkdude/edamame "0.0.11"]]

  :aliases {"lint"            ["do" ["cljfmt" "check"] ["nsorg"] ["eastwood" "{:namespaces [:source-paths]}"]]
            "lint-fix"        ["do" ["cljfmt" "fix"] ["nsorg" "--replace"]]})