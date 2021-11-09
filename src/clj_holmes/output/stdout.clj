(ns clj-holmes.output.stdout
  (:require [clojure.pprint :as pprint]))

(defn ^:private build-output-data [{:keys [findings] :as result}]
  (let [findings-rows (mapv :row findings)]
    (-> result
        (select-keys [:filename :name :message])
        (assoc :rows findings-rows))))

(defn output [result]
  (->> result
       (mapv build-output-data )
       pprint/print-table))