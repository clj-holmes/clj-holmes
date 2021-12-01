(ns clj-holmes.rules.loader.main
  (:refer-clojure :exclude [load])
  (:require [clj-holmes.rules.loader.compose :as rules.compose]
            [clj-holmes.rules.loader.utils :as rules.utils]
            [clj-holmes.specs.rule :as specs.rule]
            [clj-yaml.core :as yaml]
            [clojure.spec.alpha :as s])
  (:import (java.io File)))

(defn ^:private run-spec-validation [rule]
  (-> rule
      (assoc :valid? (s/valid? ::specs.rule/rule rule))
      (assoc :spec-message (s/explain-str ::specs.rule/rule rule))))

(defn ^:private rule-reader [^File rule-path]
  (-> rule-path
      slurp
      yaml/parse-string
      first
      rules.utils/OrderedMap->Map
      (run-spec-validation)
      (with-meta {:rule-path (-> rule-path .getAbsoluteFile str)})))

(defn ^:private read-rules [^String directory rule-tags]
  (let [all-files (-> directory File. file-seq)]
    (->> all-files
         (filter rules.utils/is-rule?)
         (map rule-reader)
         (filter #(rules.utils/filter-rules-by-tags % rule-tags)))))

(defn init! [{:keys [rule-tags rules-directory]}]
  (let [rules (read-rules rules-directory rule-tags)]
    (->> rules
         (filter :valid?)
         (pmap rules.compose/compose-rule))))

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