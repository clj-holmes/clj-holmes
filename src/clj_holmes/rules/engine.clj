(ns clj-holmes.rules.engine
  (:require [clojure.walk :as walk]))

(defn ^:private execute-rule* [forms ns-declaration {:keys [check-fn condition-fn] :as entry}]
  (if (and (map? entry) check-fn)
    (let [results (filterv #(check-fn % ns-declaration) forms)]
      (-> entry
          (assoc :result (condition-fn (-> results seq boolean)))
          (assoc :findings results)))
    entry))

(defn ^:private check* [entry]
  (if (and (map? entry) (or (contains? entry :patterns)
                            (contains? entry :patterns-either)))

    (let [kind (if (:patterns entry) :patterns :patterns-either)
          check-fn (if (= kind :patterns) every? some)
          result (boolean (check-fn (comp true? :result) (kind entry)))]
     (assoc entry :result result))
    entry))

(defn ^:private check [rule]
  (walk/postwalk check* rule))

(defn execute-rule [rule forms ns-declaration]
  (walk/postwalk (partial execute-rule* forms ns-declaration) rule))

(defn run [{:keys [forms ns-declaration]} rule]
  (let [executed-rule (execute-rule rule forms ns-declaration)
        executed-rule-checked (check executed-rule)]
    executed-rule-checked))