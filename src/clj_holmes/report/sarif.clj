(ns clj-holmes.report.sarif)

(defn ^:private sarif-boilerplate [rules]
  {:$schema "https://raw.githubusercontent.com/oasis-tcs/sarif-spec/master/Schemata/sarif-schema-2.1.0.json"
   :version "2.1.0"
   :runs    [{:tool
              {:driver {:name           "clj-holmes"
                        :informationUri "https://github.com/mthbernardes/clj-holmes"
                        :rules          (map :definition rules)}}}]})

(defn ^:private result-by-rule [{:keys [id definition findings]} filename]
  (mapv (fn [{:keys [row col end-row end-col]}]
          {:ruleId    id
           :message   {:text definition}
           :locations [{:physicalLocation
                        {:artifactLocation {:uri filename}
                         :region           {:startLine   row
                                            :endLine     end-row
                                            :startColumn col
                                            :endColumn   end-col}}}]})
        findings))

(defn ^:private scan-result->sarif-result [{:keys [rules filename]}]
  (reduce (fn [results rule]
            (concat results (result-by-rule rule filename)))
          [] rules))

(defn scans->sarif [scans rules]
  (let [results (reduce (fn [results rules]
                          (concat results (scan-result->sarif-result rules)))
                        [] scans)
        sarif-boilerplate-with-rules (sarif-boilerplate rules)]
    (when (seq results)
      (assoc-in sarif-boilerplate-with-rules [:runs 0 :results] (vec results)))))