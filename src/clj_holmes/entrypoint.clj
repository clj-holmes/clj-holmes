(ns clj-holmes.entrypoint
  (:gen-class)
  (:require [clj-holmes.engine :as engine]
            [clj-holmes.logic.sarif :as sarif]
            [clojure.data.json :as json]
            [clojure.string :as string])
  (:import (java.io File)))

(defn ^:private remove-dot-slash [filename]
  (if (string/starts-with? filename "./")
    (string/replace filename #"\./" "")
    filename))

(defn ^:private clj-file? [file]
  (and (.isFile file)
       (-> file .toString (string/includes? "project.clj") not)
       (-> file .toString (.endsWith ".clj"))))

(defn ^:private clj-files-from-directory [directory]
  (->> directory
       File.
       file-seq
       (filter clj-file?)
       (map str)
       (map remove-dot-slash)))

(defn ^:private scan [filename]
  (println filename)
  (let [code (slurp filename)
        scan-result (engine/process code)]
    (assoc scan-result :filename filename)))

(defn ^:private save-sarif! [scans directory]
  (let [sarif-report (sarif/scans->sarif scans)
        sarif-output-file (format "%s/report.sarif" directory)]
    (when sarif-report
      (->> sarif-report json/write-str (spit sarif-output-file))
      (println "Sarif report can be find in" sarif-output-file))))

(defn -main [directory]
  (let [files (clj-files-from-directory directory)
        scans-results (map scan files)]
    (save-sarif! scans-results directory)))