(ns clj-holmes.engine
  (:require [clj-holmes.diplomat.code-reader :as diplomat.code-reader]
            [clj-holmes.logic.progress :as progress]
            [clj-holmes.output.main :as output]
            [clj-holmes.rules.loader.loader :as rules.loader]
            [clj-holmes.rules.processor.processor :as rules.processor])
  (:import (java.io StringWriter)))

(defn ^:private check-rules-in-code-structure [code-structure rules progress-size]
  (let [result (->> rules
                    (pmap #(rules.processor/init! code-structure %))
                    (filterv :result))]
    (swap! progress/counter (partial + progress-size))
    result))

(defn scan* [opts]
  (let [code-structures (diplomat.code-reader/code-structure-from-clj-files-in-directory! opts)
        rules (rules.loader/init! opts)
        progress-size (progress/count-progress-size code-structures)
        scans-results (->> code-structures
                           (pmap #(check-rules-in-code-structure % rules progress-size))
                           (reduce concat))
        scan-result-output (output/output scans-results opts)]
    scans-results))

(defn scan [{:keys [verbose] :as opts}]
  (let [out (if verbose *out* (new StringWriter))]
    (binding [*out* out]
      (scan* opts))))

(comment
  (scan* {:verbose false
          :scan-path "/home/dpr/dev/clj-holmes/clj-holmes-rules/security/xxe-clojure-xml"
          :rules-directory "/home/dpr/dev/clj-holmes/clj-holmes-rules/security/xxe-clojure-xml"
          :output-type "stdout"})
  )