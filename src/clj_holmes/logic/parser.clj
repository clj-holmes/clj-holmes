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

(defn parse [require-entry]
  (when (vector? require-entry)
    (let [namespace (first require-entry)
          require-rest (rest require-entry)]
      (assoc {} namespace (apply hash-map require-rest)))))

(defn code->code-structure [forms filename]
  (let [ns-declaration (logic.namespace/find-ns-declaration forms)
        ns-name (logic.namespace/name-from-ns-declaration ns-declaration)
        requires (-> ns-declaration logic.namespace/requires logic.namespace/normalize-requires)]
    {:forms          (transduce (map (partial build-form-tree ns-name)) concat forms)
     :filename       filename
     :ns-name        ns-name
     :requires       (transduce (map parse) merge requires)
     :ns-declaration ns-declaration}))

(comment
  (code->code-structure '[(ns banana
                            (:require [clojure.edn :as edn]
                                      banana.clj
                                      [banana [jud.client] [jud.pj]]
                                      [banana.jose :refer [ac] :as j]))
                          (defn teste [x] (edn/read-string x))]
                        "banana.clj"))