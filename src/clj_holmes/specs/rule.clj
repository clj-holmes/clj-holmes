(ns clj-holmes.specs.rule
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as str]))

(s/def ::non-blank-string
  (s/and string? (complement str/blank?)))

(defn ^:private xor [x y]
  (and (or x y) (not (and x y))))

(s/def ::id ::non-blank-string)
(s/def ::name ::non-blank-string)
(s/def ::severity #{"error" "warning" "info"})
(s/def ::message ::non-blank-string)
(s/def ::precision #{"low" "medium" "high" "very-high"})
(s/def ::tags (s/coll-of ::non-blank-string))
(s/def ::properties (s/keys :req-un [::tags ::precision]))

(s/def ::pattern-not ::non-blank-string)
(s/def ::pattern ::non-blank-string)
(s/def ::function ::non-blank-string)
(s/def ::namespace ::non-blank-string)
(s/def ::custom-function? boolean?)
(s/def ::pattern-definition (s/keys :req-un [(xor ::pattern ::pattern-not)]
                                    :opt-un [::function
                                             ::namespace
                                             ::custom-function?]))

(s/def ::patterns-either (s/or :definition (s/coll-of ::pattern-definition)
                               :itself (s/coll-of (s/keys :req-un [(xor ::patterns-either ::patterns)]))))

(s/def ::patterns (s/or :definition (s/coll-of ::pattern-definition)
                        :itself (s/coll-of (s/keys :req-un [(xor ::patterns ::patterns-either)]))))

(s/def ::rule
  (s/keys :req-un [(xor ::patterns-either ::patterns)
                   ::id
                   ::name
                   ::severity
                   ::message
                   ::properties]))