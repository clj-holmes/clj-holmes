(ns clj-holmes.output.stdout-test
  (:require [clj-holmes.output.stdout :as output.stdout]
            [clj-holmes.specs.scan-result :as specs.scan-result]
            [clj-holmes.specs.stdout :as specs.stdout]
            [clojure.spec.alpha :as s]
            [clojure.test :refer :all]
            [clojure.test.check :as test.check]
            [clojure.test.check.properties :as prop]))

(deftest output
  (testing "when scan results conforms with the spec."
    (let [scan-result->stdout-output (prop/for-all [sample (s/gen ::specs.scan-result/scan-results)]
                                                   (s/valid? ::specs.stdout/results (output.stdout/output sample)))]
      (is (:result (test.check/quick-check 1 scan-result->stdout-output))))))