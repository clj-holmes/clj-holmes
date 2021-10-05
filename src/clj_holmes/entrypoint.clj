(ns clj-holmes.entrypoint
  (:gen-class)
  (:require [clj-holmes.engine :as engine]
            [clj-holmes.filesystem :as filesystem]
            [clj-holmes.sarif :as sarif]))

(defn ^:private scan [filename rules-path]
  (println filename)
  (let [code (slurp filename)
        scan-result (engine/process code rules-path)]
    (assoc scan-result :filename filename)))

(defn -main [src-directory rules-directory]
  (let [files (filesystem/clj-files-from-directory! src-directory)
        rules (filesystem/load-rules! rules-directory)
        scans-results (map #(scan % rules) files)
        sarif-report (sarif/scans->sarif scans-results rules)]
    (filesystem/save-sarif-report! sarif-report src-directory)))