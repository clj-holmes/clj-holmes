(ns clj-holmes.filesystem
  (:require [clojure.data.json :as json]
            [clojure.string :as string]
            [clojure.tools.reader.edn :as edn])
  (:import (java.io File)))

(defn ^:private remove-dot-slash [filename]
  (if (string/starts-with? filename "./")
    (string/replace filename #"\./" "")
    filename))

(defn ^:private clj-file? [file]
  (and (.isFile file)
       (-> file .toString (string/includes? "project.clj") not)
       (-> file .toString (.endsWith ".clj"))))

(defn clj-files-from-directory! [directory]
  (->> directory
       File.
       file-seq
       (filter clj-file?)
       (map str)
       (map remove-dot-slash)))

(defn load-rules! [directory]
  (->> directory
       File.
       file-seq
       (filter #(.isFile %))
       (map (comp edn/read-string slurp))))

(defn save-sarif-report! [sarif-report directory]
  (let [sarif-output-file (format "%s/report.sarif" directory)]
    (when sarif-report
      (->> sarif-report json/write-str (spit sarif-output-file))
      (println "Sarif report can be find in" sarif-output-file))))