(ns clj-holmes.rules.utils
  (:require [clj-holmes.logic.namespace :as logic.namespace]))

(defn ^:private extract-tokens-from-form [form]
  (tree-seq coll? identity form))

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
  (let [tokens (->> forms (map extract-tokens-from-form) (reduce concat))]
    (->> tokens
         (map f)
         (filterv identity)
         (mapv (fn [form]
                (assoc (meta form) :code form))))))

(comment
  (function-usage-possibilities '(ns holmes
                                   (:require [ada :as banana]))
                                'ada
                                'food))