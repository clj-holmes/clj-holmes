(ns clj-holmes.rules.utils
  (:require [clj-holmes.logic.namespace :as logic.namespace]))

; private
(defn ^:private extract-tokens-from-forms [forms]
  (->> forms
       (map #(tree-seq coll? identity %))
       (reduce concat)))

(defn ^:private enrich-form [form]
  (-> form
      meta
      (assoc :code form)))

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
       extract-tokens-from-forms
       (map f)
       (filterv identity)
       (mapv enrich-form)))

(comment
  (function-usage-possibilities '(ns holmes
                                   (:require [ada :as banana]))
                                'ada
                                'food))