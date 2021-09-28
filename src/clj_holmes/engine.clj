(ns clj-holmes.engine
  (:require [clj-holmes.config :as config]
            [clj-holmes.logic.namespace :as logic.namespace]
            [clj-holmes.logic.parser :as p]))

(defn ^:private remove-ns-from-forms [forms ns-declaration]
  (when-let [ns-declaration-index (some-> ns-declaration meta :index)]
    (-> ns-declaration-index
        (drop forms)
        vec)))

(defn ^:private parser [code]
  (let [forms (p/code->data code)
        ns-declaration (logic.namespace/find-ns-declaration forms)
        forms-without-ns (remove-ns-from-forms forms ns-declaration)]
    {:forms          (or forms-without-ns forms)
     :ns-declaration ns-declaration
     :rules []}))

(defn process [code]
  (let [code-structure (parser code)
        findings (->> config/rules
                      (mapv (fn [{:keys [entrypoint]}]
                              (entrypoint code-structure)))
                      (filterv identity))]
    (assoc code-structure :rules findings)))

(comment
  (main "(ns banana (:require [clojure.core :as eita])) (eita/read-string \"1\")"))