(ns playground
  (:require [clj-holmes.rules.utils :as utils]
            [clojure.java.io :as io]
            [clojure.spec.alpha :as s]
            [clojure.walk :as walk]
            [shape-shifter.core :refer [*wildcards* pattern->spec]]
            [tupelo.parse.yaml :as yaml]))

(defn ^:private build-custom-function [function namespace ns-declaration]
  (->> function
       symbol
       (utils/function-usage-possibilities ns-declaration (symbol namespace))
       (map (fn [element] `'~element))
       set))

(defn build-pattern-fn [{:keys [custom-function?] :as rule-pattern}]
  (let [pattern (or (:pattern rule-pattern) (:pattern-not rule-pattern))]
    (if custom-function?
      (let [{:keys [function namespace]} rule-pattern]
        (fn [form ns-declaration]
          (let [custom-function (build-custom-function function namespace ns-declaration)
                spec (binding [*wildcards* (merge *wildcards* {"$custom-function" custom-function})]
                       (pattern->spec pattern))]
            (s/valid? spec form))))
      (let [spec (pattern->spec pattern)]
        (fn [form & _]
          (s/valid? spec form))))))

(defn ^:private prepare-rule* [entry]
  (if (and (map? entry)
           (or (:pattern entry)
               (:pattern-not entry)))
    (assoc entry :check-fn (build-pattern-fn entry))
    entry))

(defn ^:private execute-rule* [forms ns-declaration entry]
  (if (and (map? entry) (:check-fn entry))
    (let [check-fn (:check-fn entry)
          results (filterv #(check-fn % ns-declaration) forms)]
      (assoc entry :results results))
    entry))

(defn prepare-rule [rule]
  (walk/prewalk prepare-rule* rule))

(defn execute-rule [rule forms ns-declaration]
  (walk/postwalk (partial execute-rule* forms ns-declaration) rule))

(defn check [rule forms ns-declaration]
  (let [executed-rule (execute-rule rule forms ns-declaration)]
    executed-rule))

(comment
  (def rule (->> "rules/xxe.yml" io/resource slurp yaml/parse first prepare-rule))
  (check rule '[(a/parse "1")] '(ns banana (:require [clojure.xml :as a]))))