(ns clj-holmes.engine
  (:require [clj-holmes.logic.namespace :as logic.namespace]
            [clj-holmes.logic.parser :as parser]
            [clj-holmes.rules.engine :as rules.engine]
            [progrock.core :as pr]
            [clj-holmes.filesystem :as filesystem]
            [clj-holmes.rules.loader :as rules.loader]
            [clj-holmes.output.main :as output]))

(def ^:private bar (atom (pr/progress-bar 100)))
(def ^:private progress-count (atom 0))

(add-watch
 progress-count
 :print (fn [_ _ _ new-state]
          (-> @bar (pr/tick new-state) pr/print)))

(defn ^:private parser [filename code]
  (let [forms (parser/code->data code)
        ns-declaration (logic.namespace/find-ns-declaration forms)]
    {:forms          (tree-seq coll? identity forms)
     :filename       filename
     :ns-declaration ns-declaration}))

(defn ^:private process [filename code rules]
  (let [code-structure (parser filename code)]
    (->> rules
         (pmap (partial rules.engine/run code-structure))
         (filterv :result))))

(defn scan-file [filename rules progress-size]
  (let [code (slurp filename)
        result (process filename code rules)]
    (swap! progress-count (partial + progress-size))
    result))

(defn scan [opts]
  (let [files (filesystem/clj-files-from-directory! opts)
        rules (rules.loader/init! opts)
        progress-size (->> files count (/ 100) float)
        scans-results (->> files
                           (pmap #(scan-file % rules progress-size))
                           (reduce concat))]
    (output/output scans-results opts)
    (shutdown-agents)))

(comment
  (def opts {:scan-path "/home/dpr/dev/nu/common-soap/"})
  (scan opts))