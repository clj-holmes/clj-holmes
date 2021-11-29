(ns clj-holmes.specs.rule
  (:require [clojure.spec.alpha :as s]))

(defn ^:private xor [x y]
  (and (or x y) (not (and x y))))

(s/def ::id string?)
(s/def ::name string?)
(s/def ::severity #{"error" "warning" "info"})
(s/def ::message string?)
(s/def ::precision #{"low" "medium" "high" "very-high"})
(s/def ::tags (s/coll-of string?))
(s/def ::properties (s/keys :req-un [::tags ::precision]))

(s/def ::pattern-not string?)
(s/def ::pattern string?)
(s/def ::function string?)
(s/def ::namespace string?)
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