(ns clj-holmes.rules.processor.processor
  (:require [clj-holmes.rules.processor.runner :as rules.runner]))

(defn ^:private adapt-findings-from-rule [finding]
  #_(->> rule
       (tree-seq coll? identity)
       (pmap :findings)
       (filter identity)
       (reduce concat)
       (into [])))

(defn init! [{:keys [forms requires filename]} loaded-rule]
  (let [executed-rule (rules.runner/execute-loaded-rule loaded-rule forms requires)]
    (if (:result executed-rule)
      (-> executed-rule
          (select-keys [:properties :name :result :id :severity :message :findings])
          (assoc :filename filename)))))