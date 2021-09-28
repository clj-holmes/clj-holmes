(ns clj-holmes.logic.sarif
  (:require [clj-holmes.config :as config]))

(def ^:private rules
  (mapv :definition config/rules))

(def ^:private sarif-boilerplate
  {:$schema "https://raw.githubusercontent.com/oasis-tcs/sarif-spec/master/Schemata/sarif-schema-2.1.0.json"
   :version "2.1.0"
   :runs    [{:tool
              {:driver {:name           "clj-holmes"
                        :informationUri "https://github.com/mthbernardes/clj-holmes"
                        :rules          rules}}}]})

(defn ^:private result-by-rule [{:keys [id definition findings]} filename]
  (mapv (fn [{:keys [row col end-row end-col]}]
          (let [uri (str "file://" filename)]
            {:ruleId    id
             :message   {:text definition}
             :locations [{:physicalLocation
                          {:artifactLocation {:uri uri}
                           :region           {:startLine   row
                                              :endLine     end-row
                                              :startColumn col
                                              :endColumn   end-col}}}]}))
        findings))

(defn ^:private scan-result->sarif-result [{:keys [rules filename]}]
  (reduce (fn [results rule]
            (concat results (result-by-rule rule filename)))
          [] rules))

(defn scans->sarif [scans]
  (let [results (reduce
                  (fn [results rules]
                    (concat results (scan-result->sarif-result rules)))
                  [] scans)]
    (when (seq results)
      (assoc-in sarif-boilerplate [:runs 0 :results] (vec results)))))