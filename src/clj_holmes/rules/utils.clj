(ns clj-holmes.rules.utils
  (:require [clj-holmes.logic.namespace :as logic.namespace]))

(defn function-usage-possibilities
  "Given the following input
  - function: read-string
  - namespace: clojure.edn
  - ns-declaration: (ns banana (:require [clojure.edn :as edn])
  The result will be a set #{read-string, clojure.edn/read-string, edn/read-string} which contains all possibilities to
  find the clojure.edn/read-string function in the namespace banana"
  [ns-declaration ns-to-find function]
  (let [requires (-> ns-declaration logic.namespace/requires)
        namespace-alias (some-> requires
                                (logic.namespace/find-ns-in-requires ns-to-find)
                                last)
        namespaced-function-with-alias (some-> namespace-alias name (symbol (name function)))
        namespaced-function (symbol (name ns-to-find) (name function))]
    (->> (conj [] function namespaced-function namespaced-function-with-alias)
         (filter identity)
         (map #(identity `'~%))
         set)))