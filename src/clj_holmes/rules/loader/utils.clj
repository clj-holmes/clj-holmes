(ns clj-holmes.rules.loader.utils
  (:require [clojure.walk :as walk]
            [clojure.string :as string]
            [clj-holmes.logic.namespace :as logic.namespace])
  (:import (flatland.ordered.map OrderedMap)
           (java.io File)))

(defn function-usage-possibilities
  "Given the following input
  - function: read-string
  - namespace: clojure.edn
  - ns-declaration: (ns banana (:require [clojure.edn :as edn])
  The result will be a set #{read-string, clojure.edn/read-string, edn/read-string} which contains all possibilities to
  find the clojure.edn/read-string function in the namespace banana"
  [ns-declaration ns-to-find function]
  ;TODO: it's one of the most expensive fn in the project
  (let [requires (-> ns-declaration logic.namespace/requires)
        namespace-alias (some-> requires
                                (logic.namespace/find-ns-in-requires ns-to-find)
                                last)
        qualified-function-with-alias (some-> namespace-alias name (symbol (name function)))
        qualified-function (symbol (name ns-to-find) (name function))]
    (->> (conj [] function qualified-function qualified-function-with-alias)
         (filter identity)
         (map #(identity `'~%))
         set)))

(defn OrderedMap->Map
  "Since yaml parse returns an ordered map it's not possible to use spec to validate the rule so it's necessary to
  transform the rule to clojure map."
  [nested-ordered-map]
  (walk/postwalk (fn [object]
                   (if (instance? OrderedMap object)
                     (into {} object)
                     object))
                 nested-ordered-map))

(defn ^:private rule-tags->vector [rules-tags]
  (if (coll? rules-tags)
    rules-tags
    (vector rules-tags)))

(defn filter-rules-by-tags [rules rule-tags]
  (if (seq rule-tags)
    (let [rule-tags (rule-tags->vector rule-tags)]
      (filterv (fn [rule]
                (let [existing-rule-tags (get-in rule [:properties :tags])]
                  (-> existing-rule-tags
                      set
                      (some rule-tags)
                      boolean)))
              rules))
    rules))

(defn is-rule? [^File file]
  (and (.isFile file)
       (-> file .getName (string/ends-with? ".yml"))))