(ns clj-holmes.output.json)

(defn ^:private extract-fields [result]
  (let [fields-to-extract [:filename :findings :name :message]]
    (select-keys result fields-to-extract)))

(defn output [results]
  (->> results
       (mapv extract-fields)))