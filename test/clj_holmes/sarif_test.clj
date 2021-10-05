(ns clj-holmes.sarif-test
  (:require [clj-holmes.sarif :as sarif]
            [clojure.test :refer :all]))

(def rule-multiple-patterns
  [{:definition {:id :example-multiple,
                 :name "example multiple",
                 :shortDescription {:text "example multiple"},
                 :fullDescription {:text "example multiple"},
                 :help {:text "example multiple"},
                 :properties {:precision :high, :tags ["rce"]}},
    :patterns [{:pattern "(+ 1 10)"}
               {:function 'slice
                :namespace 'banana,
                :check-required? true,
                :pattern "($custom-function 100)"}]}])

(deftest scans->sarif
  (testing "when there are no findings"
    (let [scan-results [{:forms          '[(b/slice (+ 1 1100)) (defn testing [args] (b/slice 1010))],
                         :ns-declaration '(ns apple (:require [banana :as b])),
                         :filename       "banana.clj"
                         :rules          []}]]
      (is (nil? (sarif/scans->sarif scan-results rule-multiple-patterns)))))

  (testing "when there are findings"
    (let [scan-results [{:forms          '[(b/slice (+ 1 10)) (defn testing [args] (b/slice 100))],
                         :ns-declaration '(ns apple (:require [banana :as b])),
                         :filename       "banana.clj"
                         :rules          [{:findings   [{:row 1, :col 47, :end-row 1, :end-col 55, :code '(+ 1 10)}
                                                        {:row 1, :col 78, :end-row 1, :end-col 91, :code '(b/slice 100)}],
                                           :id         :example,
                                           :definition "example multiple"}]}]
          expected-output {:$schema "https://raw.githubusercontent.com/oasis-tcs/sarif-spec/master/Schemata/sarif-schema-2.1.0.json",
                           :version "2.1.0",
                           :runs    [{:tool    {:driver {:name           "clj-holmes",
                                                         :informationUri "https://github.com/mthbernardes/clj-holmes",
                                                         :rules          [{:id               :example-multiple,
                                                                           :name             "example multiple",
                                                                           :shortDescription {:text "example multiple"},
                                                                           :fullDescription  {:text "example multiple"},
                                                                           :help             {:text "example multiple"},
                                                                           :properties       {:precision :high, :tags ["rce"]}}]}},
                                      :results [{:ruleId    :example,
                                                 :message   {:text "example multiple"},
                                                 :locations [{:physicalLocation {:artifactLocation {:uri "banana.clj"},
                                                                                 :region           {:startLine   1,
                                                                                                    :endLine     1,
                                                                                                    :startColumn 47,
                                                                                                    :endColumn   55}}}]}
                                                {:ruleId    :example,
                                                 :message   {:text "example multiple"},
                                                 :locations [{:physicalLocation {:artifactLocation {:uri "banana.clj"},
                                                                                 :region           {:startLine   1,
                                                                                                    :endLine     1,
                                                                                                    :startColumn 78,
                                                                                                    :endColumn   91}}}]}]}]}]
      (is (= expected-output (sarif/scans->sarif scan-results rule-multiple-patterns))))))