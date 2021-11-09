(ns clj-holmes.filesystem
  (:require [clojure.string :as string])
  (:import (java.io File)))

(defn ^:private remove-dot-slash [filename]
  (if (string/starts-with? filename "./")
    (string/replace filename #"\./" "")
    filename))

(defn ^:private clj-file? [^File file]
  (and (.isFile file)
       (-> file .toString (string/includes? "project.clj") not)
       (-> file .toString (.endsWith ".clj"))))

(defn clj-files-from-directory! [^String directory]
  (let [file-sanitize (comp remove-dot-slash str)]
    (->> directory
         File.
         file-seq
         (filter clj-file?)
         (pmap file-sanitize))))