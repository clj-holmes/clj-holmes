(ns clj-holmes.main
  (:gen-class)
  (:require [clj-holmes.engine :as engine]
            [clj-holmes.filesystem :as filesystem]
            [clj-holmes.output.main :as output]
            [cli-matic.core :as cli]
            [clj-holmes.rules.loader :as rules.loader]))

(defn main [{:keys [scan-path] :as opts}]
  (println opts)
  (let [files (filesystem/clj-files-from-directory! scan-path)
        rules (rules.loader/init! opts)
        _     (println (count rules))
        progress-size (->> files count (/ 100) float)
        scans-results (->> files
                           (pmap #(engine/scan % rules progress-size))
                           (reduce concat))]
    (output/output scans-results opts)
    (shutdown-agents)))

(def CONFIGURATION
  {:app {:command "clj-holmes"
         :description "run clj-holmes"
         :version "1.0"}
   :commands [{:command "scan"
               :description "Performs a scan for a path"
               :opts [{:option "scan-path" :short "p"
                       :type :string
                       :default :present
                       :as "Path to scan"}
                      {:option "output-file" :short "o"
                       :type :string
                       :default "clj_holmes_scan_results.txt"
                       :as "Output file"}
                      {:option "output-type" :short "t"
                       :type #{"sarif" "json" "stdout"}
                       :default "stdout"
                       :as "Output type"}
                      {:option "rule-tags" :short "r"
                       :multiple true
                       :type :string
                       :as "Only use rules with specified tags to perform the scan"}
                      {:option "ignored-paths" :short "i"
                       :type :string
                       :as "Glob for paths and files that shouldn't be scanned"}]
               :runs main}]})

(defn -main [& args]
  (cli/run-cmd args CONFIGURATION))
