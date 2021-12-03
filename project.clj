(defproject org.clojars.clj-holmes/clj-holmes "1.3.2"
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

  :dependencies [[org.clojure/clojure "1.10.2-alpha1"]
                 [org.clojure/tools.namespace "1.1.0"]
                 [org.clojure/tools.cli "1.0.206"]
                 [org.clojure/data.json "2.4.0"]
                 [cli-matic "0.4.3"]
                 [org.martinklepsch/clj-http-lite "0.4.3"]
                 [clj-commons/clj-yaml "0.7.107"]
                 [progrock "0.1.2"]
                 [org.clojars.clj-holmes/shape-shifter "0.3.6"]
                 [borkdude/edamame "0.0.15"]]

  :profiles {:dev     {:global-vars  {*warn-on-reflection* true
                                      *unchecked-math*     :warn-on-boxed}
                       :dependencies [[org.clojure/test.check "1.1.0"]]
                       :plugins      [[lein-shell "0.5.0"]]}

             :uberjar {:global-vars {*assert* false}
                       :aot         :all
                       :main        clj-holmes.main
                       :uberjar-name "clj-holmes.jar"
                       :jvm-opts    ["-Dclojure.compiler.direct-linking=true"
                                     "-Dclojure.spec.skip-macros=true"]}}

  :aliases {"native"          ["shell" "native-image" "-jar" "./target/clj-holmes.jar}" "--initialize-at-build-time"
                               "--no-fallback" "-Dclj-holmes.version=${:version}" "--native-image-info"
                               "--diagnostics-mode" "--report-unsupported-elements-at-runtime" "--verbose"
                               "--allow-incomplete-classpath"]
            "project-version" ["shell" "echo" "${:version}"]
            "clj-holmes"      ["run" "-m" "clj-holmes.entrypoint"]
            "lint"            ["do" ["cljfmt" "check"] ["nsorg"] ["eastwood" "{:namespaces [:source-paths]}"]]
            "lint-fix"        ["do" ["cljfmt" "fix"] ["nsorg" "--replace"]]})
