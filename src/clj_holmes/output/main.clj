(ns clj-holmes.output.main
  (:require [clj-holmes.output.sarif :as sarif]
            [clj-holmes.output.json :as json]
            [clj-holmes.output.stdout :as stdout]))

(defn output [results {:keys [output-file output-type]}]
  (case output-type
    "sarif"  (sarif/output results output-file)
    "json"   (json/output results output-file)
    "stdout" (stdout/output results output-file)))