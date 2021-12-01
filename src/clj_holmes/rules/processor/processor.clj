(ns clj-holmes.rules.processor.processor
  (:require [clj-holmes.rules.processor.checker :as rules.checker]
            [clj-holmes.rules.processor.runner :as rules.runner]))

(defn ^:private extract-findings-from-rule [rule]
  (->> rule
       (tree-seq coll? identity)
       (pmap :findings)
       (filter identity)
       (reduce concat)
       (into [])))

(defn init! [{:keys [forms ns-declaration filename]} loaded-rule]
  (let [executed-rule (rules.runner/execute-loaded-rule loaded-rule forms ns-declaration)
        executed-rule-checked (rules.checker/check-executed-rule executed-rule)
        findings (extract-findings-from-rule executed-rule-checked)]
    (-> executed-rule-checked
        (select-keys [:properties :name :result :id :severity :message :filename])
        (assoc :findings findings :filename filename))))