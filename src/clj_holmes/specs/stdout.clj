(ns clj-holmes.specs.stdout
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as str]))

(s/def ::non-blank-string
  (s/and string? (complement str/blank?)))

(s/def ::filename ::non-blank-string)
(s/def ::name ::non-blank-string)
(s/def ::message ::non-blank-string)
(s/def ::severity #{"error" "warning" "info"})
(s/def ::lines (s/coll-of (s/nilable pos-int?)))

(s/def ::result (s/keys :req-un [::filename
                                 ::name
                                 ::message
                                 ::severity
                                 ::lines]))

(s/def ::results (s/coll-of ::result))