(ns clj-holmes.rules.utils
  (:require [clj-holmes.logic.namespace :as logic.namespace]))

; private
(defn ^:private enrich-form [form]
  (-> form
      meta
      (assoc :code form)))

(defn ^:private apply-fn-in-all-forms [f code]
  (->> code
       (tree-seq coll? identity)
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

(defn find-in-forms [forms f]
  (let [map-apply-fn-in-all-forms (map (partial apply-fn-in-all-forms f))]
    (->> forms
         (transduce map-apply-fn-in-all-forms concat)
         (mapv enrich-form))))