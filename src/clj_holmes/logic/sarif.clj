(ns clj-holmes.logic.sarif
  (:require [clj-holmes.rules.read-string :as rule.read-string]))

(def ^:private rules
  [rule.read-string/rule])

(def ^:private sarif-boilerplate
  {:$schema "https://raw.githubusercontent.com/oasis-tcs/sarif-spec/master/Schemata/sarif-schema-2.1.0.json"
   :version "2.1.0"
   :runs    [{:tool
              {:driver {:name           "clj-holmes"
                        :informationUri "https://github.com/mthbernardes/clj-holmes"
                        :rules          rules}}}]})

(defn ^:private finding->location [{:keys [row col end-row end-col]} filename]
  (let [uri (str "file://" filename)]
    {:physicalLocation
     {:artifactLocation {:uri uri}
      :region           {:startLine   row
                         :endLine     end-row
                         :startColumn col
                         :endColumn   end-col}}}))

(defn ^:private result-by-rule [{:keys [id definition findings]} filename]
  (let [locations (mapv #(finding->location % filename) findings)]
    {:ruleId    id
     :message   {:text definition}
     :locations locations}))

(defn ^:private scan-result->sarif-result [{:keys [rules filename]}]
  (mapv #(result-by-rule % filename) rules))

(defn scans->sarif [scans]
  (let [results (->> scans
                     (map scan-result->sarif-result)
                     (reduce concat)
                     vec)]
    (assoc-in sarif-boilerplate [:runs 0 :results] results)))