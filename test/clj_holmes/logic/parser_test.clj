(ns clj-holmes.logic.parser-test
  (:require [clj-holmes.logic.parser :as parser]
            [clojure.test :refer :all]))

(deftest code->data
  (testing "when there is no ns declaration"
    (is (= '[(+ 1 1)] (parser/code->data "(+ 1 1)" "banana.clj"))))

  (testing "when there is ns declaration without requirements"
    (is (= '[(ns banana) (+ 1 1)] (parser/code->data "(ns banana) (+ 1 1)" "banana.clj"))))

  (testing "when there is ns declaration with requirements"
    (is (= '[(ns banana (:require [clojure.xml :as x])) (x/parse "a")]
           (parser/code->data "(ns banana (:require [clojure.xml :as x])) (x/parse \"a\")" "banana.clj"))))

  (testing "when there is a custom reader"
    (is (= [{:key "value"}]
           (parser/code->data "{:key #banana \"value\"}" "banana.clj")))))