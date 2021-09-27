(ns clj-holmes.rules.read-string
  (:require [clj-holmes.rules.utils :as utils]
            [clojure.core.match :refer [match]]))

(defn vulnerable? [function]
  (fn [form]
    (match [form]
           [([_ :guard function & _] :seq)] form
           [([_ :guard function] :seq)] form
           [_ :guard function] form
           :else nil)))

(defn check [{:keys [forms ns-declaration]}]
  (let [fn-to-find (utils/function-usage-possibilities ns-declaration 'clojure.core 'read-string)
        findings (utils/find-in-forms (vulnerable? fn-to-find) forms)]
    {:rule       :read-string
     :definition "Find usage of vulnerable function read-string"
     :findings   findings}))