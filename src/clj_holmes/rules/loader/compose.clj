(ns clj-holmes.rules.loader.compose
  (:require [clj-holmes.rules.loader.builder :as rules.builder]
            [clojure.walk :as walk]))

(defn ^:private is-patterns-declaration? [entry]
  (and (map? entry)
       (or (:patterns entry)
           (:patterns-either entry))))

(defn ^:private is-pattern-declaration? [entry]
  (and (map? entry)
       (or (:pattern entry)
           (:pattern-not entry))))

(defn ^:private pattern-declaration-to-fn
  "Return a function with arity of 2"
  [entry]
  (let [condition-fn (if (:pattern entry) identity not)
        check-function (rules.builder/build-pattern-fn entry)]
    (with-meta (comp condition-fn check-function) {:entry entry})))

(defn ^:private condition-fn-from-entry [entry]
  (if (:patterns-either entry)
    (fn [elements]
      (boolean (some true? elements)))
    (fn [elements]
      (every? true? elements))))

(defn ^:private patterns-declaration-to-fn [entry]
  (let [condition-fn (condition-fn-from-entry entry)
        functions (or (:patterns entry) (:patterns-either entry))]
    (comp condition-fn (apply juxt functions))))

(defn ^:private compose-rule*
  "Adds the condition-fn that'll check for the presence or absence of a pattern based on the pattern type (e.g. :pattern or :pattern-not)."
  [entry]
  (cond
    (is-pattern-declaration? entry) (pattern-declaration-to-fn entry)
    (is-patterns-declaration? entry) (patterns-declaration-to-fn entry)
    :else entry))

(defn compose-rule [rule]
  (assoc rule :check-fn (walk/postwalk compose-rule* rule)))