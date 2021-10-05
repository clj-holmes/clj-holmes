(ns clj-holmes.logic.parser-test
  (:require [clojure.test :refer :all]
            [clj-holmes.logic.parser :as parser]))

(deftest code->data
  (testing "when there is no ns declaration"
    (is (= '[(+ 1 1)] (parser/code->data "(+ 1 1)"))))

  (testing "when there is ns declaration without requirements"
    (is (= '[(ns banana) (+ 1 1)] (parser/code->data "(ns banana) (+ 1 1)"))))

  (testing "when there is ns declaration with requirements"
    (is (= '[(ns banana (:require [clojure.xml :as x])) (x/parse "a")]
           (parser/code->data "(ns banana (:require [clojure.xml :as x])) (x/parse \"a\")"))))

  (testing "when there is a custom reader"
    (is (= [{:key "value"}]
           (parser/code->data "{:key #banana \"value\"}"))))

  (testing "when there is a broken form"
    (is (thrown? Exception (parser/code->data "(+ 1 1")))))
