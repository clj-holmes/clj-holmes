(ns clj-holmes.output.stdout)

(defn ^:private build-output-data [{:keys [findings] :as result}]
  (let [findings-rows (mapv :row findings)]
    (-> result
        (select-keys [:filename :name :message :severity])
        (assoc :lines findings-rows))))

(defn output [results]
  (mapv build-output-data results))