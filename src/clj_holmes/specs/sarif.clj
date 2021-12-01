(ns clj-holmes.specs.sarif
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as str]))

(s/def ::non-blank-string
  (s/and string? (complement str/blank?)))

(s/def :driver/name #{"clj-holmes"})
(s/def :driver/informationUri #{"https://github.com/mthbernardes/clj-holmes"})

(s/def ::text ::non-blank-string)
(s/def ::id ::non-blank-string)
(s/def ::name ::non-blank-string)

(s/def ::level #{"error" "warning" "info"})
(s/def ::fullDescription (s/keys :req-un [::text]))
(s/def ::shortDescription (s/keys :req-un [::text]))
(s/def ::help (s/keys :req-un [::text]))
(s/def ::defaultConfiguration (s/keys :req-un [::level]))
(s/def ::precision #{"low" "medium" "high" "very-high"})
(s/def ::tags (s/coll-of ::non-blank-string))
(s/def ::properties (s/keys :req-un [::tags ::precision]))

(s/def ::rule (s/keys :req-un [::id
                               ::name
                               ::properties
                               ::fullDescription
                               ::shortDescription
                               ::help
                               ::defaultConfiguration]))

(s/def ::rules (s/coll-of ::rule :kind set?))

(s/def ::driver (s/keys :req-un [:driver/name
                                 :driver/informationUri
                                 ::rules]))

(s/def ::tool (s/keys :req-un [::driver]))

(s/def ::ruleId ::non-blank-string)

(s/def ::message (s/keys :req-un [::text]))
(s/def ::startLine (s/nilable pos-int?))
(s/def ::endLine (s/nilable pos-int?))
(s/def ::startColumn (s/nilable pos-int?))
(s/def ::endColumn (s/nilable pos-int?))
(s/def ::region (s/keys :req-un [::startLine
                                 ::endLine
                                 ::startColumn
                                 ::endColumn]))

(s/def ::uri ::non-blank-string)
(s/def ::artifactLocation (s/keys :req-un [::uri]))
(s/def ::physicalLocation (s/keys :req-un [::artifactLocation
                                           ::region]))

(s/def ::locations (s/coll-of (s/keys :req-un [::physicalLocation])))

(s/def ::result (s/keys :req-un [::ruleId
                                 ::message
                                 ::locations]))
(s/def ::results (s/coll-of ::result))

(s/def ::$schema #{"https://raw.githubusercontent.com/oasis-tcs/sarif-spec/master/Schemata/sarif-schema-2.1.0.json"})
(s/def ::version #{"2.1.0"})
(s/def ::runs (s/coll-of (s/keys :req-un [::tool
                                          ::results])))

(s/def ::sarif (s/keys :req-un [::$schema
                                ::version
                                ::runs]))