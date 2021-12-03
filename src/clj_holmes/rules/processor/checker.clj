(ns clj-holmes.rules.processor.checker
  (:require [clojure.walk :as walk]))

(defn ^:private pattern-type->condition-fn [pattern-type]
  (case pattern-type
    :patterns every?
    :patterns-either (comp boolean some)))

(defn ^:private entry->pattern-type [entry]
  (cond
    (contains? entry :patterns) :patterns
    (contains? entry :patterns-either) :patterns-either))

(defn ^:private check-executed-rule* [entry]
  (if (and (map? entry) (entry->pattern-type entry))
    (let [pattern-type  (entry->pattern-type entry)
          condition-fn  (pattern-type->condition-fn pattern-type)
          pattern       (pattern-type entry)
          result        (condition-fn :result pattern)]
      (assoc entry :result result))
    entry))

(defn check-executed-rule [executed-rule]
  (walk/postwalk check-executed-rule* executed-rule))
