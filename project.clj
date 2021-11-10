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

  :jvm-opts ["-Dclojure.compiler.direct-linking=true"]

  :main        clj-holmes.main

  :dependencies [[org.clojure/clojure "1.10.3"]
                 [org.clojure/tools.namespace "1.1.0"]
                 [org.clojure/tools.cli "1.0.206"]
                 [org.clojure/data.json "2.4.0"]
                 [cli-matic "0.4.3"]
                 [tupelo "21.10.06b"]
                 [progrock "0.1.2"]
                 [org.clojars.clj-holmes/shape-shifter "0.2.6"]
                 [borkdude/edamame "0.0.11"]]
  :profiles {:dev     {:global-vars {*warn-on-reflection* true
                                     *unchecked-math*     :warn-on-boxed}
                       :plugins     [[lein-shell "0.5.0"]]}

             :uberjar {:global-vars {*assert* false}
                       :aot :all
                       :jvm-opts    ["-Dclojure.compiler.direct-linking=true"
                                     "-Dclojure.spec.skip-macros=true"]}}

  :aliases {"native"     ["shell" "native-image" "--report-unsupported-elements-at-runtime"
                          "--initialize-at-build-time"
                          "-jar" "./target/${:uberjar-name:-${:name}-${:version}-standalone.jar}"
                          "-H:Name=./target/${:name}"]
            "project-version" ["shell" "echo" "${:version}"]
            "clj-holmes" ["run" "-m" "clj-holmes.entrypoint"]
            "lint"       ["do" ["cljfmt" "check"] ["nsorg"] ["eastwood" "{:namespaces [:source-paths]}"]]
            "lint-fix"   ["do" ["cljfmt" "fix"] ["nsorg" "--replace"]]})