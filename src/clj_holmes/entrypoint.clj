(ns clj-holmes.entrypoint
  (:require [clj-holmes.engine :as engine]))

(defn scan [filename]
  (let [code (slurp filename)
        scan-result (engine/process code)]
    (assoc scan-result :filename filename)))