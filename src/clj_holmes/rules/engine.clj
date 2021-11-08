(ns clj-holmes.rules.engine
  (:require [clojure.walk :as walk]))

(defn ^:private execute-rule* [forms ns-declaration {:keys [check-fn condition-fn] :as entry}]
  (if (and (map? entry) check-fn)
    (let [results (filterv #(check-fn % ns-declaration) forms)]
      (-> entry
          (assoc :result (condition-fn (-> results seq boolean)))
          (assoc :findings results)))
    entry))

(defn ^:private entry->pattern-type [entry]
  (cond
    (contains? entry :patterns) :patterns
    (contains? entry :patterns-either) :patterns-either
    :else nil))

(defn ^:private pattern-type->condition-fn [pattern-type]
  (case pattern-type
    :patterns every?
    :patterns-either (comp boolean some)))

(defn ^:private check* [entry]
  (if (and (map? entry) (entry->pattern-type entry))
    (let [pattern-type  (entry->pattern-type entry)
          condition-fn  (pattern-type->condition-fn pattern-type)
          pattern       (pattern-type entry)
          result        (condition-fn :result pattern)]
     (assoc entry :result result))
    entry))

(defn ^:private check [rule]
  (walk/postwalk check* rule))

(defn execute-rule [rule forms ns-declaration]
  (walk/postwalk (partial execute-rule* forms ns-declaration) rule))

(defn run [{:keys [forms ns-declaration]} rule]
  (let [executed-rule (execute-rule rule forms ns-declaration)
        executed-rule-checked (check executed-rule)]
    executed-rule-checked))
