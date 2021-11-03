(ns clj-holmes.rules.engine
  (:require [clj-holmes.rules.utils :as utils]
            [clojure.spec.alpha :as s]
            [shape-shifter.core :refer [*wildcards* pattern->spec]]))

(defn ^:private build-fn-to-find [function namespace ns-declaration check-required?]
  (if check-required?
    (->> function
         (utils/function-usage-possibilities ns-declaration namespace)
         (map (fn [element] `'~element))
         set)
    `#{~function}))

(defn ^:private build-spec [ns-declaration
                            {:keys [function namespace check-required? pattern] :as rule}]
  (let [fn-to-find (build-fn-to-find function namespace ns-declaration check-required?)]
    (binding [*wildcards* (merge *wildcards* {"$custom-function" fn-to-find})]
      (let [spec (pattern->spec pattern)
            check-fn (fn check [form]
                       (let [check-result (s/valid? spec form)]
                         check-result))]
        (assoc rule :check-fn check-fn)))))

(defn tap [x] (println x) x)

(defn ^:private match? [findings patterns]
  (let [findings-includes (->> findings
                               (filter :includes?)
                               (map :pattern)
                               set)
        findings-not-includes (->> findings
                                   (filter (comp not :includes?))
                                   (map :pattern)
                                   set)
        patterns-not-includes (->> patterns
                                   (filter (comp not :includes?))
                                   (map :pattern)
                                   set)]
    (and (seq (tap findings-includes))
         (or (and (empty? patterns-not-includes) (empty? findings-not-includes))
             (not= findings-not-includes patterns-not-includes)))))

(defn check [{:keys [forms ns-declaration]} {:keys [definition patterns]}]
  (let [finder (comp
                (map #(build-spec ns-declaration %))
                (map #(utils/find-in-forms forms %)))
        findings (transduce finder concat patterns)]
    (when (and (seq findings)
               (match? findings patterns))
      (assoc {} :findings (into [] findings)
             :id (:id definition)
             :definition (-> definition :shortDescription :text)))))