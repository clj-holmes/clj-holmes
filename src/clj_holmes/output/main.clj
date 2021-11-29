(ns clj-holmes.output.main
  (:require [clj-holmes.output.json :as output.json]
            [clj-holmes.output.sarif :as output.sarif]
            [clojure.data.json :as json]
            [clj-holmes.output.stdout :as output.stdout]
            [clojure.pprint :as pprint]))

(defmulti output (fn [_ {:keys [output-type]}] (keyword output-type)))

(defmethod output :sarif [results {:keys [output-file]}]
  (let [sarif-result (output.sarif/output results)]
    (spit output-file (json/write-str sarif-result))))

(defmethod output :json [results {:keys [output-file]}]
  (let [json-result (output.json/output results)]
    (spit output-file json-result)))

(defmethod output :stdout [results {:keys [output-file]}]
  (let [stdout-result (output.stdout/output results)]
    (spit output-file (with-out-str (pprint/print-table stdout-result)))))