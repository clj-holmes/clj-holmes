(ns clj-holmes.logic.reader
  (:require [clj-holmes.logic.namespace :as logic.namespace]
            [edamame.core :as edamame]))

(defn ^:private auto-resolves
  "Parses the first form and if it is a namespace declaration returns a map containing all requires aliases."
  [code]
  (let [form (edamame/parse-string code {:all true :readers (fn [_] identity)})
        namespace (logic.namespace/name-from-ns-declaration form)
        ns-requires (logic.namespace/requires form)
        normalized-ns-requires (-> ns-requires
                                   logic.namespace/normalize-requires
                                   logic.namespace/requires->auto-resolves-decl)]
    (assoc normalized-ns-requires :current namespace)))

(defn code-str->code
  "Parses a code string and returns all of its forms as data with line and column numbers metadata."
  [code filename]
  (try
    (let [auto-resolve (auto-resolves code)

          opts {:auto-resolve (fn [requested-ns]
                                (or (requested-ns auto-resolve)
                                    requested-ns))
                :all          true
                :readers      (fn [_] identity)}]
      (edamame/parse-string-all code opts))
    (catch Exception _
      (println "Impossible to parse:" filename))))