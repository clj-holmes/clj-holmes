(ns clj-holmes.diplomat.parser
  (:require [edamame.core :as edamame]
            [clj-holmes.logic.namespace :as logic.namespace])
  (:import (clojure.lang Namespace)))

(defn ^:private requires->auto-resolves-decl
  "Adapt requires from namespace declaration to a format used by edamame auto-resolve."
  [requires]
  (->> requires
       (filter (comp #(= :as %) second))
       (map #(remove keyword? %))
       (map reverse)
       (reduce concat)
       (apply hash-map)))

(defn ^:private auto-resolves
  "Parses the first form and if it is a namespace declaration returns a map containing all requires aliases."
  [file-content]
  (let [form (edamame/parse-string file-content {:all true})]
    (let [namespace (logic.namespace/name-from-ns-declaration form)
          ns-requires (logic.namespace/requires form)]
      (-> ns-requires
          requires->auto-resolves-decl
          (assoc :current namespace)))))

(defn code->data!
  "Receives a clojure file and returns all forms as data containing lines and rows metadata."
  [filepath]
  (try
    (let [file-content (slurp filepath)
          auto-resolve (auto-resolves file-content)]
      (edamame/parse-string-all file-content {:auto-resolve auto-resolve :all true}))
    (catch Exception e
      (println (format "Unable to read file %s. %s" (str filepath) (ex-message e))))))

(comment
  (def ns-decl
    (code->data! "/home/dpr/dev/nu/machete/pull-request-opener/src/pull_request_opener/entrypoint.clj")))