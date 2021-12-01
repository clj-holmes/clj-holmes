(ns clj-holmes.specs.scan-result
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as str]))

(s/def ::non-blank-string
  (s/and string? (complement str/blank?)))

(s/def ::id ::non-blank-string)
(s/def ::name ::non-blank-string)
(s/def ::result boolean?)
(s/def ::severity #{"error" "warning" "info"})
(s/def ::message ::non-blank-string)
(s/def ::filename ::non-blank-string)
(s/def ::precision #{"low" "medium" "high" "very-high"})
(s/def ::tags (s/coll-of ::non-blank-string))
(s/def ::properties (s/keys :req-un [::tags ::precision]))

(s/def ::row pos-int?)
(s/def ::col pos-int?)
(s/def ::end-row pos-int?)
(s/def ::end-col pos-int?)
(s/def ::parent (s/nilable qualified-keyword?))
(s/def ::code any?)

(s/def ::finding (s/keys :opt-un [::row
                                  ::col
                                  ::end-row
                                  ::end-col]
                         :req-un [::parent
                                  ::code]))

(s/def ::findings (s/coll-of ::finding))

(s/def ::scan-result (s/keys :req-un [::id
                                      ::name
                                      ::result
                                      ::severity
                                      ::message
                                      ::properties
                                      ::filename
                                      ::findings]))

(s/def ::scan-results (s/coll-of ::scan-result))