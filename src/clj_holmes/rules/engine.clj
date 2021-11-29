(ns clj-holmes.rules.engine
  (:require [clojure.walk :as walk]))

(defn ^:private extract-findings-from-rule [rule]
  (->> rule
       (tree-seq coll? identity)
       (filter :findings)
       (pmap :findings)
       (reduce concat)
       (into [])))

(defn ^:private execute-rule* [forms ns-declaration entry]
  (let [check-fn (:check-fn entry)
        condition-fn (:condition-fn entry)]
    (if (not (nil? check-fn))
      (let [results (filterv #(check-fn % ns-declaration) forms)
            results-with-metadata (mapv (fn [result]
                                          (assoc (meta result) :code result)) results)]
        (-> entry
            (assoc :result (condition-fn (-> results-with-metadata seq boolean)))
            (assoc :findings results-with-metadata)))
      entry)))

(defn ^:private entry->pattern-type [entry]
  (cond
    (contains? entry :patterns) :patterns
    (contains? entry :patterns-either) :patterns-either))

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

(defn ^:private execute-rule [rule forms ns-declaration]
  (walk/postwalk (partial execute-rule* forms ns-declaration) rule))

(defn run [{:keys [forms ns-declaration filename]} rule]
  (let [executed-rule (execute-rule rule forms ns-declaration)
        executed-rule-checked (check executed-rule)
        findings (extract-findings-from-rule executed-rule-checked)]
    (-> executed-rule-checked
        (select-keys [:properties :name :result :id :severity :message :filename])
        (assoc :findings findings :filename filename))))