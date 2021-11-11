(ns clj-holmes.filesystem
  (:require [clojure.string :as string])
  (:import (java.io File FileFilter)))

(defn ^:private remove-dot-slash [filename]
  (if (string/starts-with? filename "./")
    (string/replace filename #"\./" "")
    filename))

(defn ^:private clj-file? [^File file]
  (and (.isFile file)
       (-> file .toString (.endsWith ".clj"))))

(defn ^:private create-file-filter ^FileFilter [ignored-paths]
  (reify FileFilter
    (accept [_ f]
      (if (not (seq ignored-paths))
        true
        (let [ignored-paths (if (vector? ignored-paths) ignored-paths (vector ignored-paths))
              filepath (.getAbsolutePath f)]
          (->> ignored-paths
               (map #(-> % re-pattern (re-find filepath)))
               (every? nil?)))))))

(defn ^:private list-files-in-directory [^FileFilter file-filter ^String scan-path]
  (let [file (File. scan-path)]
    (tree-seq
      (fn [^File f] (. f (isDirectory)))
      (fn [^File d] (seq (. d (listFiles file-filter))))
      file)))

(defn clj-files-from-directory! [{:keys [scan-path ignored-paths]}]
  (let [file-sanitize (comp remove-dot-slash str)
        file-filter (create-file-filter ignored-paths)
        all-files-and-directories (list-files-in-directory file-filter scan-path)]
    (->> all-files-and-directories
         (filter clj-file?)
         (pmap file-sanitize))))