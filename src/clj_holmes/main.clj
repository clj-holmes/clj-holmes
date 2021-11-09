(ns clj-holmes.main
  (:gen-class)
  (:require [clj-holmes.engine :as engine]
            [clj-holmes.filesystem :as filesystem]
            [clj-holmes.output.stdout :as stdout]
            [clj-holmes.output.sarif :as sarif]
            [clj-holmes.rules.loader :as rules.loader]))

(def ^:private rules (rules.loader/init!))

(defn ^:private scan [filename]
  (println "Scanning:" filename)
  (let [code (slurp filename)]
    (engine/process filename code rules)))

(defn -main [src-directory]
  (let [files (filesystem/clj-files-from-directory! src-directory)
        scans-results (->> files (pmap scan) (reduce concat))]
    (sarif/output scans-results)
    #_(shutdown-agents)))

(comment
  (-main "/home/dpr/dev/nu/common-soap"))