(ns clj-holmes.entrypoint
  (:gen-class)
  (:require [clj-holmes.engine :as engine]
            [clj-holmes.logic.sarif :as sarif]
            [clj-holmes.filesystem :as filesystem]))

(defn ^:private scan [filename rules-path]
  (println filename)
  (let [code (slurp filename)
        scan-result (engine/process code rules-path)]
    (assoc scan-result :filename filename)))

(defn -main [src-directory rules-directory]
  (let [files (filesystem/clj-files-from-directory src-directory)
        rules (filesystem/load-rules rules-directory)
        scans-results (map #(scan % rules) files)]
    (sarif/save! scans-results src-directory)))

(comment
  (-main "/home/dpr/dev/nu/blueprinter" "resources/rules"))