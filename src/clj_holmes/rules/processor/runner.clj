(ns clj-holmes.rules.processor.runner
  (:require [clojure.walk :as walk]))

(defn ^:private add-match-code-as-meta [results]
  (pmap (fn [result]
          (assoc (meta result) :code result))
        results))

(defn ^:private execute-check-fn-in-forms [forms ns-declaration check-fn]
  (filterv (fn [form]
             (check-fn form ns-declaration))
           forms))

(defn ^:private execute-loaded-rule* [forms ns-declaration entry]
  (let [check-fn (:check-fn entry)
        condition-fn (:condition-fn entry)]
    (if (not (nil? check-fn))
      (let [results (execute-check-fn-in-forms forms ns-declaration check-fn)
            results-with-metadata (add-match-code-as-meta results)]
        (-> entry
            (assoc :result (-> results seq boolean condition-fn))
            (assoc :findings results-with-metadata)))
      entry)))

(defn execute-loaded-rule [loaded-rule forms ns-declaration]
  (walk/postwalk (partial execute-loaded-rule* forms ns-declaration) loaded-rule))