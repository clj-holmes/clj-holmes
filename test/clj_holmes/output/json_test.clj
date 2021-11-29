(ns clj-holmes.output.json-test
  (:require [clojure.test :refer :all]
            [clj-holmes.output.json :as output.json]))

(def sample [{:id         "3",
              :name       "X",
              :result     false,
              :severity   "error",
              :message    "6",
              :properties {:tags ["1" "0" "U" "9" "M6" "Z" "5" "pr0" "h"], :precision "very-high"},
              :findings   [{:end-col 1, :end-row 2, :col 2, :row 2, :parent :s/I-, :code {}}
                           {:row 2, :parent :?b/e, :code {}}
                           {:end-col 1, :col 2, :row 2, :parent :*/s, :code {}}
                           {:parent :-j/**, :code nil}]}

             {:id         "A",
              :name       "57",
              :result     false,
              :severity   "warning",
              :message    "7D",
              :properties {:tags ["D3" "O" "P" "F6"], :precision "high"},
              :findings   [{:parent nil, :code nil}
                           {:parent :*/?p+, :code nil}
                           {:end-row 2, :parent :?/e6, :code nil}
                           {:end-col 1, :end-row 1, :col 2, :row 1, :parent :I/KX, :code nil}
                           {:end-col 1, :end-row 1, :row 2, :parent :!?/p+, :code nil}]}])

(deftest output
  (testing "when the input conform the expected spec."
    (let [json-result "[{\"findings\":[{\"end-col\":1,\"end-row\":2,\"col\":2,\"row\":2,\"parent\":\"I-\",\"code\":{}},{\"row\":2,\"parent\":\"e\",\"code\":{}},{\"end-col\":1,\"col\":2,\"row\":2,\"parent\":\"s\",\"code\":{}},{\"parent\":\"**\",\"code\":null}],\"name\":\"X\",\"message\":\"6\"},{\"findings\":[{\"parent\":null,\"code\":null},{\"parent\":\"?p+\",\"code\":null},{\"end-row\":2,\"parent\":\"e6\",\"code\":null},{\"end-col\":1,\"end-row\":1,\"col\":2,\"row\":1,\"parent\":\"KX\",\"code\":null},{\"end-col\":1,\"end-row\":1,\"row\":2,\"parent\":\"p+\",\"code\":null}],\"name\":\"57\",\"message\":\"7D\"}]"]
      (is (= json-result (output.json/output sample))))))