(ns clj-holmes.rules.processor.processor
  (:require [clj-holmes.rules.processor.runner :as rules.runner]))

(defn ^:private extract-findings-from-rule [rule]
  (->> rule
       (tree-seq coll? identity)
       (pmap :findings)
       (filter identity)
       (reduce concat)
       (into [])))

(defn init! [{:keys [forms requires filename]} loaded-rule]
  (let [executed-rule (rules.runner/execute-loaded-rule loaded-rule forms requires)
        findings (extract-findings-from-rule executed-rule)]
    (if (:result executed-rule)
      (-> executed-rule
          (select-keys [:properties :name :result :id :severity :message :filename])
          (assoc :findings findings :filename filename)))))