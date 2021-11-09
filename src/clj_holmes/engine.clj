(ns clj-holmes.engine
  (:require [clj-holmes.logic.namespace :as logic.namespace]
            [clj-holmes.logic.parser :as parser]
            [clj-holmes.rules.engine :as rules.engine]))

(defn ^:private remove-ns-from-forms [forms ns-declaration]
  (when-let [ns-declaration-index (some-> ns-declaration meta :index)]
    (-> ns-declaration-index
        (drop forms)
        vec)))

(defn ^:private parser [filename code]
  (let [forms (parser/code->data code)
        ns-declaration (logic.namespace/find-ns-declaration forms)
        forms-without-ns (remove-ns-from-forms forms ns-declaration)]
    {:forms          (tree-seq coll? identity (or forms-without-ns forms))
     :filename filename
     :ns-declaration ns-declaration}))

(defn process [filename code rules]
  (let [code-structure (parser filename code)]
    (pmap (partial rules.engine/run code-structure) rules)))