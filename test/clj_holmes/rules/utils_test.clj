(ns clj-holmes.rules.utils-test
  (:require [clojure.test :refer :all]
            [clj-holmes.logic.parser :as parser]
            [clj-holmes.rules.utils :as utils]))

(deftest find-in-forms
  (testing "when there is a match in forms"
    (let [forms (parser/code->data "(+ 1 1)(- 10 39)")
          expected-output [{:code '+ :col 2 :end-col 3 :end-row 1 :row 1}
                           {:code '- :col 9 :end-col 10 :end-row 1 :row 1}]]
      (is (= expected-output (utils/find-in-forms symbol? forms)))))

  (testing "when there is not a match in forms"
    (let [forms (parser/code->data "(+ 1 1)")]
      (is (empty? (utils/find-in-forms string? forms))))))

(deftest function-usage-possibilities
  (testing "when the namespace being looked up is present in requires"
    (let [ns-declaration '(ns banana (:require [apple :as a]))]
      (is (= #{'a/slice 'slice 'apple/slice}
             (utils/function-usage-possibilities ns-declaration 'apple 'slice)))))

  (testing "when the namespace being looked up is not present in requires"
    (let [ns-declaration '(ns banana (:require [apple :as a]))]
      (is (= #{'slice 'avocado/slice}
             (utils/function-usage-possibilities ns-declaration 'avocado 'slice))))))