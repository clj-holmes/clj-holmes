(ns clj-holmes.main
  (:gen-class)
  (:require [cli-matic.core :as cli]
            [clj-holmes.engine :as engine]
            [clj-holmes.rules.loader.loader :as rules.loader]
            [clj-holmes.rules.manager :as rules.manager]))

(def CONFIGURATION
  {:app      {:command     "clj-holmes"
              :description "run clj-holmes"
              :version     (System/getProperty "clj-holmes.version")}
   :commands [{:command     "fetch-rules"
               :description "Fetch rules from an external server"
               :opts        [{:option  "repository" :short "r"
                              :type    :string
                              :default "git://clj-holmes/clj-holmes-rules#main"
                              :as      "Repository to download rules"}
                             {:option  "output-directory" :short "o"
                              :type    :string
                              :default "/tmp/clj-holmes-rules/"
                              :as      "Directory to save rules"}]
               :runs        rules.manager/fetch}
              {:command     "validate-rules"
               :description "Validate if the rules are conform expected"
               :opts        [{:option  "rules-directory" :short "d"
                              :type    :string
                              :default "/tmp/clj-holmes-rules/"
                              :as      "Directory to read rules"}
                             {:option   "rule-tags" :short "r"
                              :multiple true
                              :type     :string
                              :as       "Only use rules with specified tags to perform the scan"}]
               :runs        rules.loader/validate-rules!}
              {:command     "scan"
               :description "Performs a scan for a path"
               :opts        [{:option  "scan-path" :short "p"
                              :type    :string
                              :default :present
                              :as      "Path to scan"}
                             {:option  "rules-directory" :short "d"
                              :type    :string
                              :default "/tmp/clj-holmes-rules/"
                              :as      "Directory to read rules"}
                             {:option  "output-file" :short "o"
                              :type    :string
                              :default "clj_holmes_scan_results.txt"
                              :as      "Output file"}
                             {:option  "output-type" :short "t"
                              :type    #{"sarif" "stdout" "json"}
                              :default "stdout"
                              :as      "Output type"}
                             {:option   "rule-tags" :short "r"
                              :multiple true
                              :type     :string
                              :as       "Only use rules with specified tags to perform the scan"}
                             {:option "ignored-paths" :short "i"
                              :type   :string
                              :as     "Regex for paths and files that shouldn't be scanned"}
                             {:option  "verbose" :short "v"
                              :type    :with-flag
                              :default true
                              :as      "Enable or disable scan process feedback."}]
               :runs        engine/scan}]})

(defn -main [& args]
  (cli/run-cmd args CONFIGURATION))
