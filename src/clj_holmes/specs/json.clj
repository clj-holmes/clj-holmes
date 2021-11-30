(ns clj-holmes.specs.json
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as str]))

(s/def ::non-blank-string
  (s/and string? (complement str/blank?)))

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
(s/def ::filename ::non-blank-string)
(s/def ::name ::non-blank-string)
(s/def ::message ::non-blank-string)

(s/def ::result (s/keys :req-un [::findings
                                 ::filename
                                 ::name
                                 ::message]))

(s/def ::results (s/coll-of ::result))