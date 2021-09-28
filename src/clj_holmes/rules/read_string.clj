(ns clj-holmes.rules.read-string
  (:require [clj-holmes.rules.utils :as utils]
            [clojure.spec.alpha :as s]))

; private
(defn ^:private check-if-form-is-vulnerable
  "Returns a function that receives a form and check if it's a vulnerable pattern and return a boolean."
  [fn-to-find]
  (fn vulnerable-to-read-string? [form]
    (let [direct-invoke (s/cat :fn-to-find fn-to-find
                               :anything-else (s/* any?))
          invoke-inside-other-fn (s/cat :anything-else (s/* any?)
                                        :fn-to-find fn-to-find
                                        :rest (s/* any?))]
      (s/valid? (s/or :direct-invoke direct-invoke
                      :invoke-inside-other-fn invoke-inside-other-fn)
                form))))

; public
(def rule
  "Definition of a rule which is used by sarif."
  {:id               :read-string
   :name             "read-string serialization RCE"
   :shortDescription {:text "Usage of vulnerable function clojure.core/read-string"}
   :fullDescription  {:text "Attackers can exploit vulnerable deserialization functions which could lead to a remote code execution."}
   :help             {:text "Usage of vulnerable function clojure.core/read-string"}
   :properties       {:precision :high
                      :tags ["rce"]}})

(defn check [{:keys [forms ns-declaration]}]
  (let [fn-to-find (utils/function-usage-possibilities ns-declaration 'clojure.core 'read-string)
        findings (utils/find-in-forms (check-if-form-is-vulnerable fn-to-find) forms)]
    (when (seq findings)
      (assoc {} :findings findings :id (:id rule) :definition (-> rule :shortDescription :text)))))