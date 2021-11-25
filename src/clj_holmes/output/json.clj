(ns clj-holmes.output.json
  (:require [clojure.data.json :as json]))

(defn ^:private transform-list [_ object]
  (if (list? object)
    (str object)
    object))

(defn ^:private ->json [output]
  (json/write-str output :value-fn transform-list))

(defn ^:private extract-fields [result]
  (let [fields-to-extract [:filename :findings :name :message]]
    (select-keys result fields-to-extract )))

(defn output [results output-file]
  (let [output (map extract-fields results)
        json-output (->json output)]
    (spit output-file json-output)))