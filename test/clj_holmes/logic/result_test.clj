(ns clj-holmes.logic.result-test
  (:require [clj-holmes.logic.result :as result]
            [clojure.test :refer [deftest is testing]]))

(deftest has-errors
  (testing "when there is no error"
    (is (= false (result/has-errors? [{:severity "warning"} {:severity "info"}]))))

  (testing "when there is errors"
    (is (= true (result/has-errors? [{:severity "warning"} {:severity "error"}])))))
