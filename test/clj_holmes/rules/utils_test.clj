(ns clj-holmes.rules.utils-test
  (:require [clj-holmes.logic.parser :as parser]
            [clj-holmes.rules.utils :as utils]
            [clojure.test :refer :all]))

(deftest find-in-forms
  (testing "when there is a match in forms"
    (let [forms (parser/code->data "(+ 1 1)(- 10 39)")
          rule {:check-fn symbol? :includes? true :pattern "$symbol"}
          expected-output [{:row 1, :col 2, :end-row 1, :end-col 3, :code '+, :includes? true, :pattern "$symbol"}
                           {:row 1, :col 9, :end-row 1, :end-col 10, :code '-, :includes? true, :pattern "$symbol"}]]
      (is (= expected-output (utils/find-in-forms forms rule)))))

  (testing "when there is not a match in forms"
    (let [forms (parser/code->data "(+ 1 1)")
          rule {:check-fn string? :includes? true :pattern "$string"}]
      (is (empty? (utils/find-in-forms forms rule))))))

(deftest function-usage-possibilities
  (testing "when the namespace being looked up is present in requires"
    (let [ns-declaration '(ns banana (:require [apple :as a]))]
      (is (= #{'a/slice 'slice 'apple/slice}
             (utils/function-usage-possibilities ns-declaration 'apple 'slice)))))

  (testing "when the namespace being looked up is not present in requires"
    (let [ns-declaration '(ns banana (:require [apple :as a]))]
      (is (= #{'slice 'avocado/slice}
             (utils/function-usage-possibilities ns-declaration 'avocado 'slice))))))