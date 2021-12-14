(ns clj-holmes.rules.loader.utils
  (:require [clojure.string :as string]
            [clojure.walk :as walk]
            [flatland.ordered.map])
  (:import (flatland.ordered.map OrderedMap)
           (java.io File)))

(defn ^:private rule-filter->vector [rules-tags]
  (if (coll? rules-tags)
    rules-tags
    (vector rules-tags)))

(defn ^:private fetch-rule-property [rule location]
  (let [property (get-in rule location)]
    (if (coll? property)
      property
      (vector property))))

(defn function-usage-possibilities
  "Given the following input
  - function: read-string
  - namespace: clojure.edn
  - ns-declaration: (ns banana (:require [clojure.edn :as edn])
  The result will be a set #{read-string, clojure.edn/read-string, edn/read-string} which contains all possibilities to
  find the clojure.edn/read-string function in the namespace banana"
  [requires ns-to-find function]
  (let [namespace-alias (-> requires ns-to-find :as)
        referer (function (-> requires ns-to-find :refer set))
        qualified-function-with-alias (some-> namespace-alias name (symbol (name function)))
        qualified-function (symbol (name ns-to-find) (name function))]
    (->> (vector referer qualified-function qualified-function-with-alias)
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

(defn filter-rules-by-location [rules rule-filter location]
  (if (seq rule-filter)
    (filterv (fn [rule]
               (let [rule-properties (fetch-rule-property rule location)]
                 (-> rule-properties
                     set
                     (some rule-filter)
                     boolean)))
             rules)
    rules))

(defn is-rule? [^File file]
  (and (.isFile file)
       (-> file .getName (string/ends-with? ".yml"))))