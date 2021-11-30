(ns clj-holmes.output.stdout
  (:require [clojure.pprint :as pprint]))

(defn ^:private build-output-data [{:keys [findings] :as result}]
  (let [findings-rows (mapv :row findings)]
    (-> result
        (select-keys [:filename :name :message :severity])
        (assoc :lines findings-rows))))

(defn output [results]
  (let [output-data (mapv build-output-data results)]
    (pprint/print-table output-data)))