(ns clj-holmes.rules.loader.builder
  (:require [clj-holmes.rules.loader.utils :as utils]
            [clojure.spec.alpha :as s]
            [shape-shifter.core :refer [*config* *wildcards* pattern->spec]]))

(defn ^:private build-custom-function
  "Builds a function with a shape-shifter custom function wildcard"
  [pattern function namespace config]
  (fn [form requires]
    (let [function (symbol function)
          namespace (symbol namespace)
          custom-function (utils/function-usage-possibilities requires namespace function)
          spec (binding [*config* config
                         *wildcards* (merge *wildcards* {"$custom-function" custom-function})]
                 (pattern->spec pattern))]
      (s/valid? spec form))))

(defn ^:private build-simple-function [pattern config]
  (let [spec (binding [*config* config]
               (pattern->spec pattern))]
    (fn [form & _]
      (s/valid? spec form))))

(defn build-pattern-fn [{:keys [custom-function? interpret-regex? function namespace] :as rule-pattern}]
  (let [pattern (or (:pattern rule-pattern) (:pattern-not rule-pattern))
        config (assoc *config* :interpret-regex? interpret-regex?)]
    (if custom-function?
      (build-custom-function pattern function namespace config)
      (build-simple-function pattern config))))