(ns clj-holmes.rules.loader.builder-test
  (:require [clj-holmes.rules.loader.builder :as rules.builder]
            [clojure.test :refer :all]))

(deftest build-pattern-fn
  (testing "when it is a simple pattern"
    (let [rule {:pattern "(read-string $symbol)"}
          pattern-fn (rules.builder/build-pattern-fn rule)]
      (is (pattern-fn '(read-string x)))))

  (testing "when it is a simple pattern-not"
    (let [rule {:pattern-not "(read-string $symbol)"}
          pattern-fn (rules.builder/build-pattern-fn rule)]
      (is (pattern-fn '(read-string x)))))

  (testing "when it is a simple pattern with interpret-regex"
    (let [rule {:pattern "(#\"([a-z].*)/read-string\" \"banana\")"
                :interpret-regex? true}
          pattern-fn (rules.builder/build-pattern-fn rule)]
      (is (pattern-fn '(clojure/read-string "banana")))))

  (testing "when it is a simple pattern with interpret-regex but it does not match the input"
    (let [rule {:pattern "(#\"([a-z].*)/read-string\" \"banana\")"
                :interpret-regex? true}
          pattern-fn (rules.builder/build-pattern-fn rule)]
      (is (false? (pattern-fn '(read-string "banana"))))))

  (testing "when it is a custom pattern"
    (let [rule {:pattern "($custom-function \"banana\")"
                :namespace "clojure.core"
                :function "read-string"
                :custom-function? true}
          pattern-fn (rules.builder/build-pattern-fn rule)]
      (is (pattern-fn '(read-string "banana") '(ns banana)))))

  (testing "when it is a custom pattern but it does not match the input"
    (let [rule {:pattern "($custom-function \"banana\")"
                :namespace "clojure.core"
                :function "read-string"
                :custom-function? true}
          pattern-fn (rules.builder/build-pattern-fn rule)]
      (is (false? (pattern-fn '(clojure.banana/read-string "banana") '(ns banana))))))

  (testing "when it is a custom pattern"
    (let [rule {:pattern "($custom-function {:required (#\"\\+|-\" $number $number)})"
                :namespace "clojure.core"
                :function "read-string"
                :interpret-regex? true
                :custom-function? true}
          pattern-fn (rules.builder/build-pattern-fn rule)
          code '(c/read-string {:required (- 1 3)})
          code-namespace '(ns banana (:require [clojure.core :as c]))]
      (is (pattern-fn code code-namespace)))))