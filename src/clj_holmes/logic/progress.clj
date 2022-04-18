(ns clj-holmes.logic.progress
  (:require [progrock.core :as pr]))

(def ^:private bar (atom (pr/progress-bar 100)))

(def counter (atom 0))

(defn add-watch-to-counter []
  (add-watch counter
             :print (fn [_ _ _ new-state]
                      (-> @bar (pr/tick new-state) pr/print))))

(defn count-progress-size [files]
  (let [amount-of-files (count files)]
    (if (zero? amount-of-files)
      1
      (->> amount-of-files (/ 100) float))))
