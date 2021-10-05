(ns clj-holmes.logic.namespace-test
  (:require [clojure.test :refer :all]
            [clj-holmes.logic.namespace :as namespace]))

(deftest find-ns-declaration
  (testing "when there is not a ns declaration"
    (is (nil? (namespace/find-ns-declaration '[(+ 1 1)]))))

  (testing "when there is a ns declaration"
    (is (= '(ns banana) (namespace/find-ns-declaration '[(+ 1 1) (ns banana)])))))

(deftest requires
  (testing "when the provided data is not a ns declaration"
    (is (nil? (namespace/requires '(+ 1 1)))))

  (testing "when there is no require in the ns declaration"
    (is (empty? (namespace/requires '(ns banana)))))

  (testing "when there are requires in the ns declaration"
    (is (= '([potato :as p] [apple :as a])
           (namespace/requires '(ns banana
                                  (:require [potato :as p]
                                            [apple :as a])))))))

(deftest name-from-ns-declaration
  (testing "when there is a ns declared with a name"
    (is (= 'banana (namespace/name-from-ns-declaration '(ns banana)))))

  (testing "when there is a ns declared without a name"
    (is (= 'user (namespace/name-from-ns-declaration '(ns)))))

  (testing "when there is not a ns declared"
    (is (= 'user (namespace/name-from-ns-declaration '(+ 1 1))))))


(deftest find-ns-in-requires
  (testing "when the namespace exist in requires"
    (is (= '[apple :as a]
           (namespace/find-ns-in-requires '([potato :as p] [apple :as a]) 'apple))))

  (testing "when the namespace does not exist in requires"
    (is (nil? (namespace/find-ns-in-requires '([potato :as p] [apple :as a]) 'banana)))))