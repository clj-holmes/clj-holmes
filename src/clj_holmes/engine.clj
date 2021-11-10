(ns clj-holmes.engine
  (:require [clj-holmes.logic.namespace :as logic.namespace]
            [clj-holmes.logic.parser :as parser]
            [clj-holmes.rules.engine :as rules.engine]
            [progrock.core :as pr]))

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

(defn scan [filename rules progress-size]
  (let [code (slurp filename)
        result (process filename code rules)]
    (swap! progress-count (partial + progress-size))
    result))