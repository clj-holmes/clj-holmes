(ns playground
  (:require [clj-holmes.rules.loader.builder :as rules.builder]
            [clojure.walk :as walk]))


(defn ^:private is-patterns-declaration? [entry]
  (and (map? entry)
       (or (:patterns entry)
           (:patterns-either entry))))

(defn ^:private is-pattern-declaration? [entry]
  (and (map? entry)
       (or (:pattern entry)
           (:pattern-not entry))))

(defn ^:private pattern-declaration-to-fn
  "Return a function with arity of 2"
  [entry]
  (let [condition-fn (if (:pattern entry) identity not)
        check-function (rules.builder/build-pattern-fn entry)]
    (with-meta (comp condition-fn check-function) {:entry entry})))

(defn ^:private condition-fn-from-entry [entry]
  (if (:patterns-either entry)
    (fn [elements]
      (println "or" (type elements) elements)
      (boolean (some true? (do elements))))
    (fn [elements]
      (println "and" (type elements) elements)
      (every? true? (do elements)))))

(defn ^:private patterns-declaration-to-fn [entry]
  (let [condition-fn (condition-fn-from-entry entry)
        functions (or (:patterns entry) (:patterns-either entry))
        function-to-run (comp condition-fn (apply juxt functions))]
    function-to-run
    #_(fn validate [input ns-declaration]
        (->> functions
             (mapv (fn [function]
                     (println (class function) (meta function) input ns-declaration (function input ns-declaration))
                     (function input ns-declaration)))
             condition-fn))))

(defn ^:private compose-rule*
  "Adds the condition-fn that'll check for the presence or absence of a pattern based on the pattern type (e.g. :pattern or :pattern-not)."
  [entry]
  (cond
    (is-pattern-declaration? entry) (pattern-declaration-to-fn entry)
    (is-patterns-declaration? entry) (patterns-declaration-to-fn entry)
    :else entry))

(defn compose-rule [rule]
  (walk/postwalk compose-rule* rule))

(def rule {:id           "xxe-clojure-xml",
           :name         "Clojure xml XXE",
           :severity     "error",
           :message      "Usage of clojure xml parse",
           :properties   {:precision "medium", :tags ["xxe" "security" "vulnerability"]},
           :patterns-either     [{:patterns [{:pattern-not "(defn $& parse $& [$&] $&)"}]}
                                 {:patterns [{:function         "parse",
                                              :namespace        "clojure.xml",
                                              :custom-function? true,
                                              :pattern          "($& $custom-function $&)"}]}
                                 {:patterns-either [{:pattern-not "(.setFeature \"http://apache.org/xml/features/disallow-doctype-decl\" true)"}
                                                    {:pattern-not "(.setFeature \"http://xml.org/sax/features/external-general-entities\" false)"}
                                                    {:pattern-not "(.setFeature \"http://xml.org/sax/features/external-parameter-entities\" false)"}]}],
           :valid?       true,
           :spec-message "Success!\n"})

(comment
  (compose-rule rule)
  ((compose-rule rule) '(clojure.xml/earse baanan) '(ns sbananna (:require [clojure.xml :as x])))

  )