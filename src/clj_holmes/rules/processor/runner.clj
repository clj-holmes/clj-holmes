(ns clj-holmes.rules.processor.runner
  (:require [clj-holmes.rules.utils :as rules.utils]
            [clojure.walk :as walk]))

(defn pattern-declaration-run [{:keys [check-fn] :as pattern-declaration} forms requires]
  (let [findings (->> forms
                      (filterv #(check-fn % requires))
                      (map #(assoc (meta %) :code %)))
        result (-> findings seq boolean)]
    (-> pattern-declaration
        (assoc :findings findings)
        (assoc :result result))))

(defn pattern-not-declaration-run [{:keys [check-fn] :as pattern-declaration} forms requires]
  (let [findings (->> forms
                      (filterv #(not (check-fn % requires)))
                      (map #(assoc (meta %) :code %)))
        result (-> findings seq boolean not)]
    (-> pattern-declaration
        (assoc :findings findings)
        (assoc :result result))))

(defn patterns-declaration-run [{:keys [check-fn] :as patterns-declaration}]
  (let [patterns-executed (or (:patterns patterns-declaration) (:patterns-either patterns-declaration))]
    (-> patterns-declaration
        (assoc :result (check-fn (map :result patterns-executed)))
        (assoc :findings (->> patterns-executed (filter :result) (map :findings) (reduce concat))))))

(defn execute-loaded-rule* [entry forms requires]
  (cond
    (rules.utils/is-pattern-declaration? entry) (pattern-declaration-run entry forms requires)
    (rules.utils/is-pattern-not-declaration? entry) (pattern-not-declaration-run entry forms requires)
    (rules.utils/is-patterns-declaration? entry) (patterns-declaration-run entry)
    :else entry))

(defn execute-loaded-rule [loaded-rule forms requires]
  (walk/postwalk #(execute-loaded-rule* % forms requires) loaded-rule))