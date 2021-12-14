(ns clj-holmes.rules.loader.loader
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

(defn ^:private read-rules [^String directory rule-tags rule-severity rule-precision]
  (let [all-files (-> directory File. file-seq)
        rules (->> all-files
                   (filter rules.utils/is-rule?)
                   (map rule-reader))]
    (-> rules
        (rules.utils/filter-rules-by-location rule-tags [:properties :tags])
        (rules.utils/filter-rules-by-location rule-precision [:properties :precision])
        (rules.utils/filter-rules-by-location rule-severity [:severity]))))

(defn init! [{:keys [rule-tags rule-severity rule-precision rules-directory]}]
  (let [rules (read-rules rules-directory rule-tags rule-severity rule-precision)]
    (->> rules
         (filter :valid?)
         (pmap rules.compose/compose-rule))))

(defn validate-rules! [{:keys [rule-tags rule-severity rule-precision rules-directory]}]
  (let [rules (read-rules rules-directory rule-tags rule-severity rule-precision)
        success? (every? true? (remove :valid? rules))]
    (run! (fn [rule]
            (println (-> rule meta :rule-path))
            (println (:spec-message rule)))
          rules)
    success?))