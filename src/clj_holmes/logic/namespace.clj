(ns clj-holmes.logic.namespace
  (:require [clojure.tools.namespace.parse :refer [name-from-ns-decl ns-decl?]])
  (:import (clojure.lang LazySeq PersistentList PersistentVector)
           (java.util List)))

; private
(defn ^:private require-form? [form]
  (and (seq? form)
       (-> form first (= :require))))

(defn ^:private index-of ^long [^PersistentVector forms
                                ^PersistentList ns-declaration]
  (.indexOf forms ns-declaration))

(defn ^:private index-of-ns-declaration [forms ns-declaration]
  (inc (index-of forms ns-declaration)))

(defn ^:private is-expected-require? [requires]
  (filter #(and (coll? %)
                (= (second %) :as)) requires))

(defn ^:private alias-require? [require-declaration]
  (and (coll? require-declaration)
       (contains? (set require-declaration) :as)))

(defn ^:private merge-requires [all-requires require-entry]
  (if (instance? LazySeq require-entry)
    (concat all-requires require-entry)
    (conj all-requires require-entry)))

(defn ^:private normalize-require [require-entry]
  (if (or (symbol? require-entry)
          (not (coll? (second require-entry))))
    require-entry
    (let [[require-name & requires-rest] require-entry]
      (map (fn [require-rest]
             (let [full-namespace (symbol (str require-name "." (first require-rest)))]
               (assoc require-rest 0 full-namespace)))
           requires-rest))))

; public
(defn find-ns-declaration [forms]
  (when-let [ns-declaration (first (filter ns-decl? forms))]
    (with-meta ns-declaration {:index (index-of-ns-declaration forms ns-declaration)})))

(defn name-from-ns-declaration [form]
  (if (ns-decl? form)
    (or (name-from-ns-decl form) 'user)
    'user))

(defn requires [ns-declaration]
  (when (ns-decl? ns-declaration)
    (let [filter-map (comp (filter require-form?) (map rest) (filter is-expected-require?))]
      (transduce filter-map concat ns-declaration))))

(defn extract-parent-name-from-form-definition-function
  "Extracts the symbol name from a function definition like def, defn, defmacro and others"
  [form ns-name]
  (let [form-name (when (and (list? form)
                             (-> form second symbol?))
                    (-> form second name))]
    (when form-name
      (keyword (name ns-name) form-name))))

(defn requires->auto-resolves-decl
  "Adapt the require forms from the namespace declaration to a format used by edamame auto-resolve."
  [requires]
  (let [filter-alias-require? (filter alias-require?)
        assoc-or-return-new (fn assoc-or-return-new
                              ([new] new)
                              ([new [key & value]]
                               (let [value (vec value)
                                     alias-index (inc (.indexOf ^List value :as))
                                     alias (get value alias-index)]
                                 (assoc new alias key))))]
    (transduce filter-alias-require? assoc-or-return-new {} requires)))

(defn normalize-requires [ns-requires]
  (->> ns-requires
       (map normalize-require)
       (reduce merge-requires [])))