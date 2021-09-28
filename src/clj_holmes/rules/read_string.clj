(ns clj-holmes.rules.read-string
  (:require [clj-holmes.rules.utils :as utils]
            [clojure.spec.alpha :as s]))

(defn ^:private check-if-form-is-vulnerable [fn-to-find]
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

(def rule
  {:id               :read-string
   :name             "read-string serialization RCE"
   :shortDescription {:text "Find usage of vulnerable function read-string"}
   :fullDescription  {:text "Find usage of vulnerable function read-string"}
   :help             {:text "Find usage of vulnerable function read-string"}
   :properties       {:precision :medium}})

(defn check [{:keys [forms ns-declaration]}]
  (println "check read-string")
  (let [fn-to-find (utils/function-usage-possibilities ns-declaration 'clojure.core 'read-string)
        findings (utils/find-in-forms (check-if-form-is-vulnerable fn-to-find) forms)]
    (when (seq findings)
      (assoc {} :findings findings :id (:id rule) :definition (-> rule :shortDescription :text)))))