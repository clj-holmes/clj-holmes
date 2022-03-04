(ns clj-holmes.output.sarif)

(def version (System/getProperty "clj-holmes.version"))

(defn ^:private result->sarif-rule [{:keys [id name message severity] :as rule}]
  (let [rule (select-keys rule [:id :properties])
        sarif-rule {:name                 id
                    :shortDescription     {:text name}
                    :fullDescription      {:text message}
                    :help                 {:text message}
                    :defaultConfiguration {:level severity}}]
    (merge rule sarif-rule)))

(defn ^:private sarif-boilerplate [results]
  {:$schema "https://www.schemastore.org/schemas/json/sarif-2.1.0-rtm.5.json"
   :version "2.1.0"
   :runs    [{:tool
              {:driver {:name           "clj-holmes"
                        :informationUri "https://github.com/clj-holmes/clj-holmes"
                        :version        version
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
        sarif-results (transduce (map result->sarif-result) concat results)]
    (assoc-in sarif-boilerplate [:runs 0 :results] sarif-results)))