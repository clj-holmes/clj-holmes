(ns clj-holmes.logic.parser
  (:require [clj-holmes.config :as config]
            [clj-holmes.logic.namespace :as logic.namespace]
            [edamame.core :as edamame]))

(defn ^:private alias-require? [require-declaration]
  (->> require-declaration
       second
       (= :as)))

(defn ^:private requires->auto-resolves-decl
  "Adapt requires from namespace declaration to a format used by edamame auto-resolve."
  [requires]
  (->> requires
       (filter alias-require?)
       (reduce (fn [new [value _ key]]
                 (assoc new key value)) {})))

(defn ^:private auto-resolves
  "Parses the first form and if it is a namespace declaration returns a map containing all requires aliases."
  [code]
  (let [form (edamame/parse-string code {:all true})
        namespace (logic.namespace/name-from-ns-declaration form)
        ns-requires (logic.namespace/requires form)]
    (-> ns-requires
        requires->auto-resolves-decl
        (assoc :current namespace))))

(defn code->data
  "Receives a clojure file and returns all forms as data containing lines and rows metadata."
  [code]
  (let [auto-resolve (auto-resolves code)
        opts {:auto-resolve auto-resolve
              :all          true
              :readers      (fn [_] identity)}]
    (edamame/parse-string-all code opts)))

(comment
  (code->data "(ns banana) (+ 1 1)"))