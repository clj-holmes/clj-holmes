(ns clj-holmes.rules.loader.utils-test
  (:require [clj-holmes.rules.loader.utils :as loader.utils]
            [clj-yaml.core :as yaml]
            [clojure.test :refer :all]))

(deftest function-usage-possibilities
  (testing "when the namespace being looked up is present in requires"
    (let [requires {'apple  'a}]
      (is (= #{(quote 'a/slice) (quote 'apple/slice) (quote 'slice)}
             (loader.utils/function-usage-possibilities requires 'apple 'slice)))))

  (testing "when the namespace being looked up is not present in requires"
    (let [requires {'apple 'a}]
      (is (= #{(quote 'slice) (quote 'avocado/slice)}
             (loader.utils/function-usage-possibilities requires 'avocado 'slice)))))

  (testing "when the there are no requires"
    (let [requires {}]
      (is (= #{(quote 'slice) (quote 'avocado/slice)}
             (loader.utils/function-usage-possibilities requires 'avocado 'slice))))))

(deftest filter-rule-by-tags
  (testing "when there is no tags"
    (let [rules [{:properties {:tags ["banana"]}}]]
      (is (= rules (loader.utils/filter-rules-by-tags rules nil)))))

  (testing "when there is a tag and a rule with this tag"
    (let [rules [{:properties {:tags ["banana"]}}
                 {:properties {:tags ["apple"]}}]
          expected-output [{:properties {:tags ["banana"]}}]]
      (is (= expected-output (loader.utils/filter-rules-by-tags rules "banana")))))

  (testing "when there is a rule with more than one tag"
    (let [rules [{:properties {:tags ["banana" "pie"]}}
                 {:properties {:tags ["apple"]}}]
          expected-output [{:properties {:tags ["banana" "pie"]}}]]
      (is (= expected-output (loader.utils/filter-rules-by-tags rules "banana")))))

  (testing "when there is a rule with more than one tag"
    (let [rules [{:properties {:tags ["banana" "pie"]}}
                 {:properties {:tags ["apple"]}}]]
      (is (= rules (loader.utils/filter-rules-by-tags rules ["banana" "apple"])))))

  (testing "when there is a tag but no rules"
    (let [rules []]
      (is (empty? (loader.utils/filter-rules-by-tags rules ["banana" "apple"]))))))

(deftest OrderedMap->Map
  (testing "when the provided input is a ordered-map"
    (let [nested-ordered-map (yaml/parse-string "- properties:\n    precision: medium\n    tags:\n      - correctness\n")
          expected-output [{:properties {:precision "medium", :tags ["correctness"]}}]]
      (is (= expected-output (loader.utils/OrderedMap->Map nested-ordered-map))))))