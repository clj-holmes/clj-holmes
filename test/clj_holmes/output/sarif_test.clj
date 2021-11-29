(ns clj-holmes.output.sarif-test
  (:require [clojure.test :refer :all]
            [clojure.test.check :as test.check]
            [clj-holmes.specs.scan-result :as specs.scan-result]
            [clojure.test.check.properties :as prop]
            [clj-holmes.specs.sarif :as specs.sarif]
            [clj-holmes.output.sarif :as output.sarif]
            [clojure.spec.alpha :as s]))

(deftest output
  (testing "when scan results conforms with the spec."
    (let [scan-result->sarif-output (prop/for-all [sample (s/gen ::specs.scan-result/scan-results)]
                                      (s/valid? ::specs.sarif/sarif (output.sarif/output sample)))]
      (is (:result (test.check/quick-check 10 scan-result->sarif-output))))))