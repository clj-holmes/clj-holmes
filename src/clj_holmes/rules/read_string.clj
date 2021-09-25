(ns clj-holmes.rules.read-string
  (:require [clojure.core.match :refer [match]]))

(defn form-vulnerable? [form function]
  (match [form]
         [([_ :guard function  & _] :seq)] form
         [([_ :guard function] :seq)] form
         [_ :guard function] form
         :else nil))

(defn check [{:keys [forms ns-declaration]}]
  :not-implemented)