(ns clj-holmes.main
  (:require [clj-holmes.engine :as engine]
            [clj-holmes.filesystem :as filesystem]
            [clj-holmes.rules.loader :as rules.loader]))

(def ^:private rules (rules.loader/init!))

(defn ^:private scan [filename]
  (println "Scanning: " filename)
  (let [code (slurp filename)]
    (engine/process code rules)))

(defn -main [src-directory]
  (let [files (filesystem/clj-files-from-directory! src-directory)
        scans-results (pmap scan files)]
    scans-results))