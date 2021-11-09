(ns clj-holmes.rules.loader
  (:require [clj-holmes.rules.utils :as utils]
            [clojure.java.io :as io]
            [clojure.spec.alpha :as s]
            [clojure.string :as string]
            [clojure.walk :as walk]
            [shape-shifter.core :refer [*wildcards* pattern->spec]]
            [tupelo.parse.yaml :as yaml])
  (:import (java.io File)))

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

(defn ^:private build-condition-fn [condition]
  (case condition
    :and (fn [& elements]
           (every? identity elements))
    :not (partial not)))

(defn ^:private prepare-rule* [entry]
  (if (and (map? entry)
           (or (:pattern entry)
               (:pattern-not entry)))
    (let [condition (if (:pattern entry) :and :not)
          condition-fn (build-condition-fn condition)]
      (-> entry
          (assoc :condition-fn condition-fn)
          (assoc :check-fn (build-pattern-fn entry))))
    entry))

(defn ^:private prepare-rule [rule]
  (walk/prewalk prepare-rule* rule))

(defn ^:private is-rule? [^File file]
  (and (.isFile file)
       (-> file .getName (string/ends-with? ".yml"))))

(defn ^:private local-rules []
  (let [reader (comp first yaml/parse slurp)]
    (->> "rules"
         io/resource
         .getFile
         File.
         file-seq
         (filter is-rule?)
         (map reader))))

(defn init! []
  (map prepare-rule (local-rules)))