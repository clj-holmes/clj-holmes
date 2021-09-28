(ns clj-holmes.config
  (:require [clj-holmes.rules.read-string :as rule.read-string]))

(def rules
  "Rules definition used all over clj-holmes."
  [{:entrypoint rule.read-string/check
    :definition rule.read-string/rule}])

(def readers
  "List of custom readers."
  {'nu/time identity
   'nu/date identity})