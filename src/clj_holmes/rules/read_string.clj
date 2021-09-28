(ns clj-holmes.rules.read-string
  (:require [clj-holmes.rules.utils :as utils]
            [clojure.spec.alpha :as s]))

(defn check-if-form-is-vulnerable [fn-to-find]
  (fn vulnerable-to-read-string [form]
    (when (s/valid? (s/or
                      :direct-invoke
                      (s/cat :fn-to-find fn-to-find
                             :anything-else (s/* any?))

                      :invoke-inside-other-fn
                      (s/cat :anything-else (s/* any?)
                             :fn-to-find fn-to-find
                             :rest (s/* any?)))
                    form)
      form)))

(defn check [{:keys [forms ns-declaration]}]
  (let [fn-to-find (utils/function-usage-possibilities ns-declaration 'clojure.core 'read-string)
        findings (utils/find-in-forms (check-if-form-is-vulnerable fn-to-find) forms)]
    {:rule       :read-string
     :definition "Find usage of vulnerable function read-string"
     :findings   findings}))