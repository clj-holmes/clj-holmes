(ns clj-holmes.rules.processor.runner
  (:require [clojure.walk :as walk]
            [clj-holmes.rules.utils :as rules.utils]))

(defn tap [x] (println x) x)

(defn pattern-declaration-run [{:keys [check-fn] :as pattern-declaration} forms requires]
  (let [findings (->> forms
                      (filterv #(not (check-fn % requires)))
                      (map #(assoc (meta %) :code %))
                      tap)
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

(comment
  (def rule {:properties   {:precision "medium", :tags ["xxe" "security" "vulnerability"]},
             :valid?       true,
             :spec-message "Success!\n",
             :patterns     [{:patterns [{:function         "parse",
                                         :namespace        "clojure.xml",
                                         :custom-function? true,
                                         :pattern          "($& $custom-function $&)"}]}
                            {:patterns-either [{:pattern-not "(.setFeature \"http://apache.org/xml/features/disallow-doctype-decl\" true)"}
                                               {:pattern-not "(.setFeature \"http://xml.org/sax/features/external-general-entities\" false)"}
                                               {:pattern-not "(.setFeature \"http://xml.org/sax/features/external-parameter-entities\" false)"}]}],
             :name         "Clojure xml XXE",
             :id           "xxe-clojure-xml",
             :severity     "error",
             :message      "Usage of clojure xml parse"})

  (def composed-rule (clj-holmes.rules.loader.compose/compose-rule rule))

  (execute-loaded-rule composed-rule ['(clojure.xml/parse "banana")
                                      '(.setFeature "http://xml.org/sax/features/external-general-entities" false)
                                      '(.setFeature "http://xml.org/sax/features/external-parameter-entities" false)]
                       nil))