(ns clj-holmes.rules.loader.compose
  (:require [clj-holmes.rules.loader.builder :as rules.builder]
            [clj-holmes.rules.utils :as rules.utils]
            [clojure.walk :as walk]))

(defn ^:private pattern-declaration-to-fn
  "Return a function with arity of 2"
  [entry]
  (let [condition-fn (if (:pattern entry) identity not)
        check-function (rules.builder/build-pattern-fn entry)]
    (comp condition-fn check-function)))

(defn ^:private condition-fn-from-entry [entry]
  (if (:patterns-either entry)
    (fn [elements]
      (boolean (some true? elements)))
    (fn [elements]
      (every? true? elements))))

(defn ^:private patterns-declaration-to-fn [entry]
  (let [condition-fn (condition-fn-from-entry entry)]
    condition-fn))

(defn ^:private compose-rule*
  "Adds the condition-fn that'll check for the presence or absence of
  a pattern based on the pattern type (e.g. :pattern or :pattern-not)."
  [entry]
  (cond
    (rules.utils/is-any-pattern-declaration? entry) (assoc entry :check-fn (pattern-declaration-to-fn entry))
    (rules.utils/is-patterns-declaration? entry) (assoc entry :check-fn (patterns-declaration-to-fn entry))
    :else entry))

(defn compose-rule [rule]
  (walk/postwalk compose-rule* rule))