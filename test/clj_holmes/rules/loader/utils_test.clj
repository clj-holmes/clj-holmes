(ns clj-holmes.rules.loader.utils-test
  (:require [clj-holmes.rules.loader.utils :as loader.utils]
            [clj-yaml.core :as yaml]
            [clojure.test :refer :all]))

(deftest function-usage-possibilities
  (testing "when the namespace being looked up is present in requires"
    (let [requires {'apple {:as 'a}}]
      (is (= #{(quote 'a/slice) (quote 'apple/slice)}
             (loader.utils/function-usage-possibilities requires 'apple 'slice)))))

  (testing "when the namespace being looked up is not present in requires"
    (let [requires {'apple 'a}]
      (is (= #{(quote 'avocado/slice)}
             (loader.utils/function-usage-possibilities requires 'avocado 'slice)))))

  (testing "when the there is a namespace and refer to the looked function"
    (let [requires '{avocado {:as v :refer [slice]}}]
      (is (= #{(quote 'slice) (quote 'v/slice) (quote 'avocado/slice)}
             (loader.utils/function-usage-possibilities requires 'avocado 'slice)))))

  (testing "when the there are no requires"
    (let [requires {}]
      (is (= #{(quote 'avocado/slice)}
             (loader.utils/function-usage-possibilities requires 'avocado 'slice))))))

(deftest filter-rule-by-location
  (testing "when there is no tags"
    (let [rules [{:properties {:tags ["banana"]}}]]
      (is (= rules (loader.utils/filter-rules-by-location rules nil [:properties :tags])))))

  (testing "when there is a tag and a rule with this tag"
    (let [rules [{:properties {:tags ["banana"]}}
                 {:properties {:tags ["apple"]}}]
          expected-output [{:properties {:tags ["banana"]}}]]
      (is (= expected-output (loader.utils/filter-rules-by-location rules ["banana"] [:properties :tags])))))

  (testing "when there is a precision and a rule with this precision"
    (let [rules [{:properties {:precision "high"}}
                 {:properties {:tags ["apple"]}}]
          expected-output [{:properties {:precision "high"}}]]
      (is (= expected-output (loader.utils/filter-rules-by-location rules ["high"] [:properties :precision])))))

  (testing "when there is a severity and a rule with this severity"
    (let [rules [{:severity "error"
                  :properties {:precision "high"}}
                 {:properties {:tags ["apple"]}}]
          expected-output [{:properties {:precision "high"}
                            :severity "error"}]]
      (is (= expected-output (loader.utils/filter-rules-by-location rules ["error"] [:severity])))))

  (testing "when there is a rule with more than one tag"
    (let [rules [{:properties {:tags ["banana" "pie"]}}
                 {:properties {:tags ["apple"]}}]
          expected-output [{:properties {:tags ["banana" "pie"]}}]]
      (is (= expected-output (loader.utils/filter-rules-by-location rules ["banana"] [:properties :tags])))))

  (testing "when there is a rule with more than one tag"
    (let [rules [{:properties {:tags ["banana" "pie"]}}
                 {:properties {:tags ["apple"]}}]]
      (is (= rules (loader.utils/filter-rules-by-location rules ["banana" "apple"] [:properties :tags])))))

  (testing "when there is a tag but no rules"
    (let [rules []]
      (is (empty? (loader.utils/filter-rules-by-location rules ["banana" "apple"] [:properties :tags]))))))

(deftest OrderedMap->Map
  (testing "when the provided input is a ordered-map"
    (let [nested-ordered-map (yaml/parse-string "- properties:\n    precision: medium\n    tags:\n      - correctness\n")
          expected-output [{:properties {:precision "medium", :tags ["correctness"]}}]]
      (is (= expected-output (loader.utils/OrderedMap->Map nested-ordered-map))))))