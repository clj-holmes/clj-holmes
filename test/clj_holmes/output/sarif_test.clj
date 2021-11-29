(ns clj-holmes.output.sarif-test
  (:require [clojure.test :refer :all]
            [clj-holmes.specs.scan-result :as specs.scan-result]
            [clj-holmes.specs.sarif :as specs.sarif]
            [clj-holmes.output.sarif :as output.sarif]
            [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]))

(deftest output
  (testing "when scan results conforms with the spec."
    (let [sample (gen/sample (s/gen ::specs.scan-result/scan-results) 10)]


      (s/valid? ::specs.sarif/sarif (output.sarif/output sample)))))

(clojure.test.check/)