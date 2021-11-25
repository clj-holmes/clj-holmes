(ns clj-holmes.usage-tree
  (:require [clj-holmes.rules.engine :as engine]
            [clj-holmes.rules.loader :as loader]))

(defn build-rule [{{:keys [namespace reference]} :parent}]
  (println namespace reference)
  (let [rule {:custom-function? true
              :namespace        namespace
              :function         reference
              :pattern          "($& $custom-function $&)"
              :condition-fn     (fn [& elements]
                                  (every? identity elements))}]
    (assoc rule :check-fn (loader/build-pattern-fn rule))))

(defn run [rule code-structure]
  (when (not= (str (:ns-name code-structure)) (:namespace rule))
    (let [findings (:findings (engine/run code-structure rule))]
      (when (seq findings)
        findings))))

(defn find-references [codes rule]
  (->> codes
       (pmap (partial run rule))
       (filter identity)
       (reduce concat)))

(defn main [findings codes]
  (def findings findings)
  (def codes codes))


(comment
  (loop [current-tree-of-call {:start [(-> findings first :findings first build-rule)]}
         tree-of-call {}]
    (let [findings (reduce concat (vals current-tree-of-call))
          usages (reduce (fn [current-tree-of-call rule]
                           (let [namespaced-fn (keyword (:namespace rule) (:function rule))
                                 find-usages (mapv build-rule (find-references codes rule))]
                             (assoc current-tree-of-call namespaced-fn find-usages)))
                         {} findings)]
      usages)))
*e

(apply identity (remove nil? [nil nil]))