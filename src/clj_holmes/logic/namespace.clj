(ns clj-holmes.logic.namespace
  (:require [clojure.tools.namespace.parse :refer [ns-decl? name-from-ns-decl]]))

; private
(defn ^:private require-form? [form]
  (and (seq? form)
       (-> form first (= :require))))

(defn ^:private index-of-ns-declaration [forms ns-declaration]
  (-> forms (.indexOf ns-declaration) inc))

; public
(defn find-ns-declaration [forms]
  (when-let [ns-declaration (->> forms
                                 (filter ns-decl?)
                                 first)]
    (with-meta ns-declaration {:index (index-of-ns-declaration forms ns-declaration)})))

(defn name-from-ns-declaration [form]
  (if (ns-decl? form)
    (or (name-from-ns-decl form) 'user)
    'user))

(defn requires [ns-declaration]
  (when (ns-decl? ns-declaration)
    (let [requires-decl (filter require-form? ns-declaration)]
      (->> requires-decl
           (map rest)
           (reduce concat)))))

(defn find-ns-in-requires [requires namespace]
  (->> requires
       (filter (comp #(= namespace %) first))
       first))