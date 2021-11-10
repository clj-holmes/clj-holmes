(ns clj-holmes.output.sarif
  (:require [clojure.data.json :as json]))

(defn ^:private result->sarif-rule [{:keys [message severity] :as rule}]
  (let [rule (select-keys [:id :name :properties] rule)
        sarif-rule {:fullDescription      {:text message}
                    :shortDescription     {:text message}
                    :help                 {:text message}
                    :defaultConfiguration {:level severity}}]
    (merge rule sarif-rule)))

(defn ^:private sarif-boilerplate [results]
  {:$schema "https://raw.githubusercontent.com/oasis-tcs/sarif-spec/master/Schemata/sarif-schema-2.1.0.json"
   :version "2.1.0"
   :runs    [{:tool
              {:driver {:name           "clj-holmes"
                        :informationUri "https://github.com/mthbernardes/clj-holmes"
                        :rules          (set (pmap result->sarif-rule results))}}}]})

(defn ^:private result->sarif-result [{:keys [id message findings filename]}]
  (pmap (fn [{:keys [row col end-row end-col]}]
          {:ruleId    id
           :message   {:text message}
           :locations [{:physicalLocation
                        {:artifactLocation {:uri filename}
                         :region           {:startLine   row
                                            :endLine     end-row
                                            :startColumn col
                                            :endColumn   end-col}}}]})
        findings))

(defn output [results]
  (let [sarif-boilerplate (sarif-boilerplate results)
        sarif-results (transduce (map result->sarif-result) concat results) #_(reduce concat)
        sarif-report (assoc-in sarif-boilerplate [:runs 0 :results] sarif-results)]
    (spit "/tmp/result.json" (json/write-str sarif-report))))