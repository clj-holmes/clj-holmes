(ns clj-holmes.output.sarif-test
  (:require [clj-holmes.output.sarif :as output.sarif]
            [clj-holmes.specs.sarif :as specs.sarif]
            [clj-holmes.specs.scan-result :as specs.scan-result]
            [clojure.spec.alpha :as s]
            [clojure.test :refer :all]
            [clojure.test.check :as test.check]
            [clojure.test.check.properties :as prop]))

(deftest output
  (testing "when scan results conforms with the spec."
    (let [scan-result->sarif-output (prop/for-all [sample (s/gen ::specs.scan-result/scan-results)]
                                                  (s/valid? ::specs.sarif/sarif (output.sarif/output sample)))]
      (is (:result (test.check/quick-check 10 scan-result->sarif-output))))))