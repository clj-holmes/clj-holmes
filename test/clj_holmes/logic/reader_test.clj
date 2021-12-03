(ns clj-holmes.logic.reader-test
  (:require [clj-holmes.logic.reader :as reader]
            [clojure.test :refer :all]))

(deftest code->data
  (testing "when there is no ns declaration"
    (is (= '[(+ 1 1)] (reader/code-str->code "(+ 1 1)" "banana.clj"))))

  (testing "when there is ns declaration without requirements"
    (is (= '[(ns banana) (+ 1 1)] (reader/code-str->code "(ns banana) (+ 1 1)" "banana.clj"))))

  (testing "when there is ns declaration with requirements"
    (is (= '[(ns banana (:require [clojure.xml :as x])) (x/parse "a")]
           (reader/code-str->code "(ns banana (:require [clojure.xml :as x])) (x/parse \"a\")" "banana.clj"))))

  (testing "when there is a custom reader"
    (is (= [{:key "value"}]
           (reader/code-str->code "{:key #banana \"value\"}" "banana.clj")))))