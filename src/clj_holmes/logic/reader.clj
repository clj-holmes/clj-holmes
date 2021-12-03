(ns clj-holmes.logic.reader
  (:require [clj-holmes.logic.namespace :as logic.namespace]
            [edamame.core :as edamame])
  (:import (clojure.lang LazySeq)))

(defn ^:private alias-require? [require-declaration]
  (and (coll? require-declaration)
       (contains? (set require-declaration) :as)))

(defn ^:private requires->auto-resolves-decl
  "Adapt requires from namespace declaration to a format used by edamame auto-resolve."
  [requires]
  (let [filter-alias-require? (filter alias-require?)
        assoc-or-return-new (fn assoc-or-return-new
                              ([new] new)
                              ([new [key & value]]
                               (let [value (vec value)
                                     alias-index (inc (.indexOf value :as))
                                     alias (get value alias-index)]
                                 (assoc new alias key))))]
    (transduce filter-alias-require? assoc-or-return-new {} requires)))

(defn ^:private normalize-require [require-entry]
  (if (or (symbol? require-entry)
          (not (coll? (second require-entry))))
    require-entry
    (let [[require-name & requires-rest] require-entry]
      (map (fn [require-rest]
             (let [full-namespace (symbol (str require-name "." (first require-rest)))]
               (assoc require-rest 0 full-namespace)))
           requires-rest))))

(defn ^:private merge-requires [all-requires require-entry]
  (if (instance? LazySeq require-entry)
    (concat all-requires require-entry)
    (conj all-requires require-entry)))

(defn ^:private auto-resolves
  "Parses the first form and if it is a namespace declaration returns a map containing all requires aliases."
  [code]
  (let [form (edamame/parse-string code {:all true :readers (fn [_] identity)})
        namespace (logic.namespace/name-from-ns-declaration form)
        ns-requires (logic.namespace/requires form)
        normalized-ns-requires (->> ns-requires
                                    (map normalize-require)
                                    (reduce merge-requires []))]
    (-> normalized-ns-requires
        requires->auto-resolves-decl
        (assoc :current namespace))))

(defn code-str->code
  "Receives a clojure file and returns all forms as data containing lines and rows metadata."
  [code filename]
  (try
    (let [auto-resolve (auto-resolves code)
          opts {:auto-resolve auto-resolve
                :all          true
                :readers      (fn [_] identity)}]
      (edamame/parse-string-all code opts))
    (catch Exception _
      (println "Impossible to parse:" filename))))