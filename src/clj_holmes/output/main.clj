(ns clj-holmes.output.main
  (:require [clj-holmes.output.json :as output.json]
            [clj-holmes.output.sarif :as output.sarif]
            [clj-holmes.output.stdout :as output.stdout]
            [clojure.data.json :as json]
            [clojure.pprint :as pprint])
  (:import (java.io OutputStreamWriter)))

(defmulti output (fn [_ {:keys [output-type]}] (keyword output-type)))

(defmethod output :sarif [results {:keys [output-file]}]
  (let [sarif-result (output.sarif/output results)]
    (spit output-file (json/write-str sarif-result))))

(defmethod output :json [results {:keys [output-file]}]
  (let [json-result (output.json/output results)
        json-str (json/write-str json-result :value-fn (fn [_ value]
                                                         (if (list? value)
                                                           (str value)
                                                           value)))]
    (spit output-file json-str)))

(defmethod output :stdout [results _]
  (let [stdout-result (output.stdout/output results)]
    (binding [*out* (OutputStreamWriter. System/out)]
      (pprint/print-table stdout-result))))