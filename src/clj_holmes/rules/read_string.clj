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
   :shortDescription {:text "Usage of vulnerable function clojure.core/read-string"}
   :fullDescription  {:text "Attackers can exploit vulnerable deserialization functions which could lead to a remote code execution."}
   :help             {:text "Usage of vulnerable function clojure.core/read-string"}
   :properties       {:precision :medium
                      :security-severity 8.0
                      :problem {:severity :error}}})

(defn check [{:keys [forms ns-declaration]}]
  (let [fn-to-find (utils/function-usage-possibilities ns-declaration 'clojure.core 'read-string)
        findings (utils/find-in-forms (check-if-form-is-vulnerable fn-to-find) forms)]
    (when (seq findings)
      (assoc {} :findings findings :id (:id rule) :definition (-> rule :shortDescription :text)))))