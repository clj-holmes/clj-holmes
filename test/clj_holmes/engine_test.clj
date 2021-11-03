(ns clj-holmes.engine-test
  (:require [clj-holmes.engine :as engine]
            [clojure.test :refer :all]))

(def rule-single-pattern
  [{:definition {:id :example,
                 :name "example single",
                 :shortDescription {:text "example single"},
                 :fullDescription {:text "example single"},
                 :help {:text "example single"},
                 :properties {:precision :high :tags ["rce"]}},
    :patterns [{:function 'slice
                :namespace 'banana
                :check-required? true
                :includes? true
                :pattern "(defn $& [$&] ($custom-function $string))"}]}])

(def rule-multiple-patterns
  [{:definition {:id :example,
                 :name "example multiple",
                 :shortDescription {:text "example multiple"},
                 :fullDescription {:text "example multiple"},
                 :help {:text "example multiple"},
                 :properties {:precision :high, :tags ["rce"]}},
    :patterns [{:pattern "(+ 1 10)"
                :includes? true}
               {:function 'slice
                :namespace 'banana,
                :check-required? true,
                :includes? true
                :pattern "($custom-function 100)"}]}])

(deftest process
  (testing "when there is a match with a single pattern"
    (let [code "(ns apple (:require [banana :as b])) (b/slice 10) (defn testing [args] (b/slice \"slice\"))"
          expected-output {:forms '[(b/slice 10) (defn testing [args] (b/slice "slice"))],
                           :ns-declaration '(ns apple (:require [banana :as b])),
                           :rules [{:findings [{:row 1,
                                                :col 51,
                                                :end-row 1,
                                                :end-col 90,
                                                :code '(defn testing [args] (b/slice "slice")),
                                                :includes? true,
                                                :pattern "(defn $& [$&] ($custom-function $string))"}],
                                    :id :example,
                                    :definition "example single"}]}]
      (is (= expected-output (engine/process code rule-single-pattern)))))

  (testing "when there are matches with multiples patterns"
    (let [code "(ns apple (:require [banana :as b])) (b/slice (+ 1 10)) (defn testing [args] (b/slice 100))"
          expected-output {:forms          '[(b/slice (+ 1 10)) (defn testing [args] (b/slice 100))]
                           :ns-declaration '(ns apple (:require [banana :as b]))
                           :rules          [{:definition "example multiple"
                                             :findings   [{:code      '(+ 1 10)
                                                           :col       47
                                                           :end-col   55
                                                           :end-row   1
                                                           :includes? true
                                                           :pattern   "(+ 1 10)"
                                                           :row       1}
                                                          {:code      '(b/slice 100)
                                                           :col       78
                                                           :end-col   91
                                                           :end-row   1
                                                           :includes? true
                                                           :pattern   "($custom-function 100)"
                                                           :row       1}]
                                             :id         :example}]}]
      (is (= expected-output (engine/process code rule-multiple-patterns)))))

  (testing "when there is not a match with a single pattern"
    (let [code "(ns apple (:require [banana :as b])) (b/slice 10) (defn testing [args] (b/slice 100))"
          expected-output {:forms          '[(b/slice 10) (defn testing [args] (b/slice 100))],
                           :ns-declaration '(ns apple
                                              (:require [banana :as b])),
                           :rules          []}]
      (is (= expected-output (engine/process code rule-single-pattern)))))

  (testing "when there are not matches with multiples patterns"
    (let [code "(ns apple (:require [banana :as b])) (b/slice (+ 1 11)) (defn testing [args] (b/slice 101))"
          expected-output {:forms '[(b/slice (+ 1 11)) (defn testing [args] (b/slice 101))],
                           :ns-declaration '(ns apple (:require [banana :as b])),
                           :rules []}]
      (is (= expected-output (engine/process code rule-multiple-patterns))))))