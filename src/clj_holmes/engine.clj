(ns clj-holmes.engine
  (:require [clj-holmes.filesystem :as filesystem]
            [clj-holmes.output.main :as output]
            [clj-holmes.rules.engine :as rules.engine]
            [clj-holmes.rules.loader :as rules.loader]
            [progrock.core :as pr])
  (:import (java.io StringWriter)))

(def ^:private bar (atom (pr/progress-bar 100)))
(def ^:private progress-count (atom 0))

(add-watch
 progress-count
 :print (fn [_ _ _ new-state]
          (-> @bar (pr/tick new-state) pr/print)))

(defn ^:private count-progress-size [files]
  (let [amount-of-files (count files)]
    (if (zero? amount-of-files)
      1
      (->> amount-of-files (/ 100) float))))

(defn ^:private check-rules-in-code-structure [code-structure rules progress-size]
  (let [run (partial rules.engine/run code-structure)
        result (->> rules (pmap run) (filterv :result))]
    (swap! progress-count (partial + progress-size))
    result))

(defn scan* [opts]
  (let [code-structures (filesystem/code-structure-from-clj-files-in-directory! opts)
        rules (rules.loader/init! opts)
        progress-size (count-progress-size code-structures)
        scans-results (->> code-structures
                           (pmap #(check-rules-in-code-structure % rules progress-size))
                           (reduce concat))]
    (output/output scans-results opts)))

(defn scan [{:keys [verbose] :as opts}]
  (let [out (if verbose *out* (new StringWriter))]
    (binding [*out* out]
      (scan* opts))))