(ns clj-holmes.logic.parser
  (:require [clj-holmes.logic.namespace :as logic.namespace]))

(defn ^:private add-parent-node-meta [parent child]
  (if (meta child)
    (vary-meta child assoc :parent parent)
    child))

(defn ^:private build-form-tree [ns-name form]
  (let [qualified-parent-name (logic.namespace/extract-parent-name-from-form-definition-function form ns-name)]
    (->> form
         (tree-seq coll? identity)
         (pmap (partial add-parent-node-meta qualified-parent-name)))))

(defn ^:private parse-requires [requires-map require-entry]
  (cond
    (symbol? require-entry) (assoc requires-map require-entry nil)
    (and (vector? require-entry)
         (= :as (second require-entry))) (assoc requires-map (first require-entry) (last require-entry))
    :else requires-map))

(defn code->code-structure [forms filename]
  (let [ns-declaration (logic.namespace/find-ns-declaration forms)
        ns-name (logic.namespace/name-from-ns-declaration ns-declaration)
        requires (logic.namespace/requires ns-declaration)]
    {:forms          (transduce (map (partial build-form-tree ns-name)) concat forms)
     :filename       filename
     :ns-name        ns-name
     :requires       (reduce parse-requires {} requires)
     :ns-declaration ns-declaration}))

(comment
  (code->code-structure '[(ns banana
                            (:require [clojure.edn :as edn]
                                      banana.clj
                                      [banana.jose :refer [ac]]))
                          (defn teste [x] (edn/read-string x))]
                        "banana.clj"))