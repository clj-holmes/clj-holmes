(ns clj-holmes.rules.loader
  (:refer-clojure :exclude [load])
  (:require [clj-holmes.rules.utils :as utils]
            [clj-holmes.specs.rule :as specs.rule]
            [clj-yaml.core :as yaml]
            [clojure.spec.alpha :as s]
            [clojure.string :as string]
            [clojure.walk :as walk]
            [shape-shifter.core :refer [*config* *wildcards* pattern->spec]])
  (:import (flatland.ordered.map OrderedMap)
           (java.io File)))

(defn ^:private custom-function-possibilities [function namespace ns-declaration]
  (->> function
       symbol
       (utils/function-usage-possibilities ns-declaration (symbol namespace))
       (map (fn [element] `'~element))
       set))

(defn ^:private build-custom-function [pattern function namespace config]
  (fn [form ns-declaration]
    (let [custom-function (custom-function-possibilities function namespace ns-declaration)
          spec (binding [*config* config
                         *wildcards* (merge *wildcards* {"$custom-function" custom-function})]
                 (pattern->spec pattern))]
      (s/valid? spec form))))

(defn ^:private build-simple-function [pattern config]
  (let [spec (binding [*config* config]
               (pattern->spec pattern))]
    (fn [form & _]
      (s/valid? spec form))))

(defn ^:private build-pattern-fn [{:keys [custom-function? interpret-regex? function namespace] :as rule-pattern}]
  (let [pattern (or (:pattern rule-pattern) (:pattern-not rule-pattern))
        config (assoc *config* :interpret-regex? interpret-regex?)]
    (if custom-function?
      (build-custom-function pattern function namespace config)
      (build-simple-function pattern config))))

(defn ^:private build-condition-fn [condition]
  (case condition
    :and (fn [& elements]
           (every? identity elements))
    :not not))

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

(defn ^:private OrderedMap->Map
  "Since yaml parse returns an ordered map it's not possible to use spec to validate the rule so it's necessary to
  transform the rule to clojure map."
  [rule]
  (walk/postwalk (fn [object]
                   (if (instance? OrderedMap object)
                     (into {} object)
                     object))
                 rule))

(defn ^:private rule-reader [^File rule-path]
  (let [read-rule (->> rule-path
                       slurp
                       yaml/parse-string
                       first
                       OrderedMap->Map)]
    (with-meta read-rule {:rule-path (-> rule-path .getAbsoluteFile str)})))

(defn ^:private filter-rule-by-tags [rule-tags rules]
  (if (seq rule-tags)
    (filter (fn [rule]
              (let [existing-rule-tags (get-in rule [:properties :tags])]
                (-> existing-rule-tags
                    set
                    (some rule-tags)
                    boolean)))
            rules)
    rules))

(defn ^:private check-if-rule-is-valid [rule]
  (-> rule
      (assoc :valid? (s/valid? ::specs.rule/rule rule))
      (assoc :spec-message (s/explain-str ::specs.rule/rule rule))))

(defn ^:private read-rules [^String directory rule-tags]
  (->> directory
       File.
       file-seq
       (filter is-rule?)
       (map rule-reader)
       set
       (filter-rule-by-tags rule-tags)
       (map check-if-rule-is-valid)))

(defn init! [{:keys [rule-tags rules-directory]}]
  (let [rules (read-rules rules-directory rule-tags)]
    (->> rules
         (filter :valid?)
         (pmap prepare-rule))))

(defn validate-rules! [{:keys [rule-tags rules-directory]}]
  (let [rules (read-rules rules-directory rule-tags)
        success? (every? true? (filter (complement :valid?) rules))]
    (run! (fn [rule]
            (println (-> rule meta :rule-path))
            (println (:spec-message rule)))
          rules)
    success?))

(comment
  (init! {:rules-directory "/tmp/clj-holmes-rules"})
  (validate-rules! {:rules-directory "/tmp/clj-holmes-rules"}))