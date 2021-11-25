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

(defn ^:private build-custom-function [function namespace ns-declaration]
  (->> function
       symbol
       (utils/function-usage-possibilities ns-declaration (symbol namespace))
       (map (fn [element] `'~element))
       set))

(defn ^:private build-pattern-fn [{:keys [custom-function? interpret-regex?] :as rule-pattern}]
  (let [pattern (or (:pattern rule-pattern) (:pattern-not rule-pattern))]
    (if custom-function?
      (let [{:keys [function namespace]} rule-pattern]
        (fn [form ns-declaration]
          (let [custom-function (build-custom-function function namespace ns-declaration)
                spec (binding [*config* (assoc *config* :interpret-regex? (boolean interpret-regex?))
                               *wildcards* (merge *wildcards* {"$custom-function" custom-function})]
                       (pattern->spec pattern))]
            (s/valid? spec form))))
      (let [spec (binding [*config* (assoc *config* :interpret-regex? interpret-regex?)]
                   (pattern->spec pattern))]
        (fn [form & _]
          (s/valid? spec form))))))

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

(defn ^:private OrderedMap->Map [rule]
  (walk/postwalk (fn [object]
                   (if (instance? OrderedMap object)
                     (into {} object)
                     object))
                 rule))

(defn ^:private rule-reader [rule-path]
  (let [read-rule (->> rule-path
                       slurp
                       yaml/parse-string
                       first
                       OrderedMap->Map)]
    (with-meta read-rule {:rule-path (.getName rule-path)})))

(defn ^:private read-rules [^String directory]
  (->> directory
       File.
       file-seq
       (filter is-rule?)
       (map rule-reader)
       set))

(defn ^:private filter-rule-by-tags [rule-tags rules]
  (if (seq rule-tags)
    (filter (fn [rule]
              (let [existing-rule-tags (get-in rule [:properties :tags])]
                (boolean (some (set existing-rule-tags) rule-tags))))
            rules)
    rules))

(defn init! [{:keys [rule-tags rules-directory]}]
  (->> rules-directory
       read-rules
       (filter-rule-by-tags rule-tags)
       (pmap prepare-rule)))

(defn validate-rules! [{:keys [rule-tags rules-directory]}]
  (let [invalid-rules (->> rules-directory
                           read-rules
                           (filter-rule-by-tags rule-tags)
                           (filter #((complement s/valid?) ::specs.rule/rule %)))]
    (when (seq invalid-rules)
      (println (format "The following rules do not conform to the spec: %s"
                       (mapv #(-> % meta :rule-path) invalid-rules)))
      (run! #(s/explain ::specs.rule/rule %) invalid-rules)
      false)))

(comment
  (init! {:rules-directory "/tmp/clj-holmes-rules"})
  (validate-rules! {:rules-directory "/tmp/clj-holmes-rules"}))
