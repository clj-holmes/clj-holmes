(ns clj-holmes.output.json-test
  (:require [clj-holmes.output.json :as output.json]
            [clj-holmes.specs.json :as specs.json]
            [clj-holmes.specs.scan-result :as specs.scan-result]
            [clojure.spec.alpha :as s]
            [clojure.test :refer :all]
            [clojure.test.check :as test.check]
            [clojure.test.check.properties :as prop]))

(deftest output
  (testing "when scan results conforms with the spec."
    (let [scan-result->json-output (prop/for-all [sample (s/gen ::specs.scan-result/scan-results)]
                                                 (s/valid? ::specs.json/results (output.json/output sample)))]
      (is (:result (test.check/quick-check 15 scan-result->json-output))))))