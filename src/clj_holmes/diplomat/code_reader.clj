(ns clj-holmes.diplomat.code-reader
  (:require [clj-holmes.logic.parser :as parser]
            [clj-holmes.logic.reader :as reader]
            [clojure.string :as string])
  (:import (java.io File FileFilter)))

(defn ^:private remove-dot-slash [filename]
  (if (string/starts-with? filename "./")
    (string/replace filename #"\./" "")
    filename))

(defn ^:private clj-file? [^File file]
  (and (.isFile file)
       (-> file .toString (.endsWith ".clj"))))

(defn ^:private prepare-ignored-paths [ignored-paths]
  (when ignored-paths
    (let [ignored-paths (if (vector? ignored-paths)
                          ignored-paths
                          (vector ignored-paths))]
      (map re-pattern ignored-paths))))

(defn ^:private create-file-filter ^FileFilter [ignored-paths]
  (let [ignored-paths (prepare-ignored-paths ignored-paths)]
    (reify FileFilter
      (accept [_ f]
        (if (nil? ignored-paths)
          true
          (let [filepath (.getAbsolutePath f)]
            (->> ignored-paths
                 (map (fn [pattern]
                        (re-find pattern filepath)))
                 (every? nil?))))))))

(defn ^:private list-files-in-directory [^FileFilter file-filter ^String scan-path]
  (let [file (File. scan-path)]
    (tree-seq
     (fn [^File f] (.isDirectory f))
     (fn [^File d] (seq (.listFiles d file-filter)))
     file)))

(defn ^:private file->code-structure [filename]
  (let [str-code (slurp filename)
        code (reader/code-str->code str-code filename)]
    (parser/code->code-structure code filename)))

(defn code-structure-from-clj-files-in-directory! [{:keys [scan-path ignored-paths]}]
  (let [file-sanitize (comp remove-dot-slash str)
        file-filter (create-file-filter ignored-paths)
        all-files-and-directories (list-files-in-directory file-filter scan-path)]
    (->> all-files-and-directories
         (filter clj-file?)
         (pmap file-sanitize)
         (pmap file->code-structure))))