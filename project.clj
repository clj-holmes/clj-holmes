(defproject org.clojars.clj-holmes/clj-holmes "0.2.11"
  :description "Clojure SAST."
  :url "https://github.com/clj-holmes/clj-holmes"
  :scm {:name "git"
        :url "https://github.com/clj-holmes/clj-holmes"}
  :license {:name "Eclipse Public License 1.0"
            :url "http://opensource.org/licenses/eclipse-1.0.php"}

  :deploy-repositories [["clojars" {:url "https://clojars.org/repo"
                                    :username :env/clojars_user
                                    :password :env/clojars_pass
                                    :sign-releases false}]]

  :plugins [[lein-ancient "0.6.15"]
            [lein-cljfmt "0.6.4"]
            [lein-nsorg "0.3.0"]
            [jonase/eastwood "0.3.10"]]

  :source-paths ["src/"]

  :test-paths ["test/"]

  :resource-paths ["resources/"]

  :main        clj-holmes.main
  :jvm-opts ["-Dclojure.compiler.direct-linking=true"]

  :dependencies [[org.clojure/clojure "1.10.3"]
                 [org.clojure/tools.namespace "1.1.0"]
                 [org.clojure/data.json "2.4.0"]
                 [tupelo "21.10.06b"]
                 [org.clojars.clj-holmes/shape-shifter "0.2.6"]
                 [borkdude/edamame "0.0.11"]]
  :profiles {:dev {:global-vars {*warn-on-reflection* true
                                 *unchecked-math* :warn-on-boxed}}
             :uberjar {:global-vars {*assert* false}
                       :jvm-opts    ["-Dclojure.compiler.direct-linking=true"
                                     "-Dclojure.spec.skip-macros=true"]
                       :aot         :all}}

  :aot :all

  :aliases {"clj-holmes" ["run" "-m" "clj-holmes.entrypoint"]
            "lint"       ["do" ["cljfmt" "check"] ["nsorg"] ["eastwood" "{:namespaces [:source-paths]}"]]
            "lint-fix"   ["do" ["cljfmt" "fix"] ["nsorg" "--replace"]]})
