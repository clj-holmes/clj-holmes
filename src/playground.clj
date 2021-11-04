(ns playground
  (:require [clj-yaml.core :as yaml]
            [clojure.java.io :as io]))

;escrita ;leitura ;manutenção

{:id :xxe-clojure-xml
 :name "Clojure xml XXE"
 :severity :error
 :properties {:precision :medium
              :tags ["xxe" "security" "vulnerability"]}
 :patterns [{:condition :and
             :patterns [{:pattern "(defn $& parse $& [$&] $&)"
                         :condition :not}]}
            {:condition :and
             :patterns [{:pattern "($custom-function $&)"
                         :condition :and}]}
            {:condition :and
             :patterns [{:pattern "(.setFeature \"http://apache.org/xml/features/disallow-doctype-decl\" true)"
                         :condition :not}
                        {:pattern "(.setFeature \"http://apache.org/xml/features/disallow-doctype-decl\" true)"
                         :condition :not}
                        {:pattern "(.setFeature \"http://apache.org/xml/features/disallow-doctype-decl\" true)"
                         :condition :not}]}]}

{:id :xxe-clojure-xml
 :name "Clojure xml XXE"
 :severity :error
 :message ""
 :properties {:precision :medium
              :tags ["xxe" "security" "vulnerability"]}
 :patterns [{:patterns [{:not-pattern "(defn $& parse $& [$&] $&)"}]}
            {:patterns [{:function 'parse
                         :namespace 'clojure.xml
                         :build-custom-function? true
                         :pattern "($custom-function $&)"}]}
            {:patterns-either [{:not-pattern "(.setFeature \"http://apache.org/xml/features/disallow-doctype-decl\" true)"}
                               {:not-pattern "(.setFeature \"http://apache.org/xml/features/disallow-doctype-decl\" true)"}
                               {:not-pattern "(.setFeature \"http://apache.org/xml/features/disallow-doctype-decl\" true)"}]}]}

(-> "rules/xxe.yml"
    io/resource
    slurp
    yaml/parse-string
    first)
