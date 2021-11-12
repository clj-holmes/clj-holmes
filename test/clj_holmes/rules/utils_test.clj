(ns clj-holmes.rules.utils-test
  (:require [clj-holmes.rules.utils :as utils]
            [clojure.test :refer :all]))

(deftest function-usage-possibilities
  (testing "when the namespace being looked up is present in requires"
    (let [ns-declaration '(ns banana (:require [apple :as a]))]
      (is (= #{'a/slice 'slice 'apple/slice}
             (utils/function-usage-possibilities ns-declaration 'apple 'slice)))))

  (testing "when the namespace being looked up is not present in requires"
    (let [ns-declaration '(ns banana (:require [apple :as a]))]
      (is (= #{'slice 'avocado/slice}
             (utils/function-usage-possibilities ns-declaration 'avocado 'slice))))))