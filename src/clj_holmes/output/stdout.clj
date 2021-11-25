(ns clj-holmes.output.stdout
  (:require [clojure.pprint :as pprint]))

(defn ^:private build-output-data [{:keys [findings] :as result}]
  (let [findings-rows (mapv :row findings)]
    (-> result
        (select-keys [:filename :name :message :severity])
        (assoc :lines findings-rows))))

(defn output [results output-file]
  (let [output-data (mapv build-output-data results)]
    (pprint/print-table output-data)
    (spit output-file (with-out-str (pprint/print-table output-data)))))