(ns clj-holmes.rules.utils
  (:require [clj-holmes.logic.namespace :as logic.namespace]))

; private
(defn ^:private enrich-form [form]
  (-> form
      meta
      (assoc :code form)))

(defn ^:private apply-fn-in-all-forms [code f]
  (->> code
       (tree-seq coll? identity)
       (filter coll?)
       (filter f)))

; public
(defn function-usage-possibilities [ns-declaration ns-to-find function]
  (let [requires (-> ns-declaration logic.namespace/requires)
        namespace-alias (some-> requires
                                (logic.namespace/find-ns-in-requires ns-to-find)
                                last)
        namespaced-function-with-alias (some-> namespace-alias name (symbol (name function)))
        namespaced-function (symbol (name ns-to-find) (name function))]
    (->> (conj [] function namespaced-function namespaced-function-with-alias)
         (filter identity)
         set)))

(defn find-in-forms [f forms]
  (->> forms
       (map #(apply-fn-in-all-forms % f))
       (reduce concat)
       (mapv enrich-form)))