(ns clj-holmes.rules.engine
  (:require [clojure.walk :as walk]))

(defn ^:private not-empty? [value]
  (not (empty? value)))

(defn ^:private execute-rule* [forms ns-declaration entry]
  (if (and (map? entry) (:check-fn entry))
    (let [check-fn (:check-fn entry)
          results (filterv #(check-fn % ns-declaration) forms)]
      (assoc entry :results results))
    entry))

(defn ^:private check* [entry]

  )

(defn check [rule]
  (walk/postwalk check* rule))

(defn execute-rule [rule forms ns-declaration]
  (walk/postwalk (partial execute-rule* forms ns-declaration) rule))

(defn run [{:keys [forms ns-declaration]} rule]
  (let [executed-rule (execute-rule rule forms ns-declaration)]
    executed-rule))