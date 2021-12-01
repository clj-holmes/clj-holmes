(ns clj-holmes.logic.namespace
  (:require [clojure.tools.namespace.parse :refer [name-from-ns-decl ns-decl?]])
  (:import (clojure.lang PersistentList PersistentVector)))

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
      (->> ns-declaration
           (transduce filter-map concat)))))

(defn find-ns-in-requires [requires namespace]
  (let [is-namespace? (comp #(= namespace %) first)]
    (first (filter is-namespace? requires))))

(defn extract-parent-name-from-form-definition-function
  "Extracts the symbol name from a function definition like def, defn, defmacro and others"
  [form ns-name]
  (let [form-name (when (and (list? form)
                             (-> form second symbol?))
                    (-> form second name))]
    (when form-name
      (keyword (name ns-name) form-name))))