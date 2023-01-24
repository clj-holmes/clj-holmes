(ns clj-holmes.engine
  (:require [clj-holmes.diplomat.code-reader :as diplomat.code-reader]
            [clj-holmes.output.main :as output]
            [clj-holmes.rules.loader.loader :as rules.loader]
            [clj-holmes.rules.processor.processor :as rules.processor])
  (:import (java.io StringWriter)))

(defn ^:private check-rules-in-code-structure [code-structure rules]
  (let [result (->> rules
                    (pmap #(rules.processor/init! code-structure %))
                    (filterv :result))]

    result))

(defn scan* [opts]
  (let [code-structures (diplomat.code-reader/code-structure-from-clj-files-in-directory! opts)
        rules (rules.loader/init! opts)
        scans-results (->> code-structures
                           (pmap #(check-rules-in-code-structure % rules))
                           (reduce concat))
        scan-result-output (output/output scans-results opts)]
    scan-result-output))

(defn scan [{:keys [verbose fail-on-result] :as opts}]
  (let [out (if verbose *out* (new StringWriter))]
    (binding [*out* out]
      (let [result (scan* opts)]
        (when fail-on-result
          (count result))))))
