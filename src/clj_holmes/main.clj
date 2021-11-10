(ns clj-holmes.main
  (:gen-class)
  (:require [clj-holmes.engine :as engine]
            [clj-holmes.filesystem :as filesystem]
            [clj-holmes.output.stdout :as stdout]
            [clj-holmes.output.sarif :as sarif]
            [clj-holmes.rules.loader :as rules.loader]))

(defn -main [src-directory]
  (let [files (filesystem/clj-files-from-directory! src-directory)
        rules (rules.loader/init!)
        progress-size (->> files count (/ 100) float)
        scans-results (->> files
                           (pmap #(engine/scan % rules progress-size))
                           (reduce concat))]
    (sarif/output scans-results)
    (shutdown-agents)))