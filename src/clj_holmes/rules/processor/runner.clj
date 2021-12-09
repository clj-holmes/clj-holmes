(ns clj-holmes.rules.processor.runner)

(defn ^:private add-match-code-as-meta [results]
  (pmap (fn [result]
          (assoc (meta result) :code result))
        results))

(defn ^:private execute-check-fn-in-forms [forms requires check-fn]
  (filterv (fn [form]
             (check-fn form requires))
           forms))

(defn execute-loaded-rule [{:keys [check-fn] :as loaded-rule} forms requires]
  (let [findings (->> forms
                      (filterv (fn [form]
                                 (check-fn form requires)))
                      add-match-code-as-meta)
        result (-> findings seq boolean)]
    (-> loaded-rule
        (assoc :result result)
        (assoc :findings findings))))