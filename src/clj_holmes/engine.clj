(ns clj-holmes.engine
  (:require [clj-holmes.filesystem :as filesystem]
            [clj-holmes.logic.namespace :as logic.namespace]
            [clj-holmes.logic.parser :as parser]
            [clj-holmes.output.main :as output]
            [clj-holmes.rules.engine :as rules.engine]
            [clj-holmes.rules.loader :as rules.loader]
            [progrock.core :as pr]))

(def ^:private bar (atom (pr/progress-bar 100)))
(def ^:private progress-count (atom 0))

(add-watch
 progress-count
 :print (fn [_ _ _ new-state]
          (-> @bar (pr/tick new-state) pr/print)))

(defn ^:private add-parent-node-meta [parent child]
  (if (meta child)
    (vary-meta child assoc :parent parent)
    child))

(defn ^:private build-form-tree [form]
  (->> form
       (tree-seq coll? identity)
       (map (partial add-parent-node-meta form))))

(defn ^:private parser [filename]
  (let [code (slurp filename)
        forms (parser/code->data filename code)
        ns-declaration (logic.namespace/find-ns-declaration forms)]
    {:forms          (transduce (map build-form-tree) concat forms)
     :filename       filename
     :ns-declaration ns-declaration}))

(defn ^:private count-progress-size [files]
  (let [amount-of-files (count files)]
    (if (zero? amount-of-files)
      1
      (->> amount-of-files (/ 100) float))))

(defn ^:private process [filename rules]
  (let [code-structure (parser filename)]
    (->> rules
         (pmap (partial rules.engine/run code-structure))
         (filterv :result))))

(defn scan-file [filename rules progress-size]
  (let [result (process filename rules)]
    (swap! progress-count (partial + progress-size))
    result))

(defn scan [opts]
  (let [files (filesystem/clj-files-from-directory! opts)
        rules (rules.loader/init! opts)
        progress-size (count-progress-size files)
        scans-results (->> files
                           (mapv #(scan-file % rules progress-size))
                           (reduce concat))]
    (output/output scans-results opts)))