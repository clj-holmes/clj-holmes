(ns clj-holmes.logic.result)

(defn has-errors? [scans-results]
  (-> #(= (:severity %) "error")
      (filter scans-results)
      count
      (> 0)))
