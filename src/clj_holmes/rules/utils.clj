(ns clj-holmes.rules.utils
  (:require [clj-holmes.logic.namespace :as logic.namespace]
            [clojure.zip :as z]))

; private
(defn ^:private enrich-form [form]
  (-> form
      meta
      (assoc :code form)))

(defn ^:private apply-fn-in-all-forms [code f]
  (loop [zip (z/seq-zip code)
         matches []]
    (let [[form location] zip]
      (if (= :end location)
        matches
        (if (coll? form)
          (when-let [new-matches (some->> form f (conj matches))]
            (recur (z/next zip) new-matches))
          (recur (z/next zip) matches))))))

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
       (filter identity)
       (reduce concat)
       (mapv enrich-form)))