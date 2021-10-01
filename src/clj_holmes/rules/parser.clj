(ns clj-holmes.rules.parser
  (:require [instaparse.core :as insta]
            [clojure.spec.alpha :as s]
            [clojure.string :as string]))

(defn ^:private random-char []
  (-> 26 rand (+ 65) char))

(defn ^:private random-keyword []
  (->> random-char
       repeatedly
       (take 5)
       (apply str)
       string/lower-case
       keyword))

(def ^:private parser
  (-> "resources/grammar.ebfn" slurp insta/parser))

(def wildcards
  {:fn `(s/cat :function #(= 'fn %)
               :args (s/coll-of symbol? :kind vector?)
               :body any?)
   :symbol `symbol?
   :string `string?
   :char `char?
   :keyword `keyword?
   :map `map?
   :number `number?
   :list `list?
   :vector `vector?
   :regex `s/regex?})

(defmulti transform (fn [[key _]]  key))

(defmethod transform :sexpr [[ _ & values]]
  (->> values
       (reduce (fn [new value]
                 (let [key (random-keyword)]
                   (conj new key (transform value))))
               [])
       (cons `s/cat)
       list
       (cons `s/spec)))

(defmethod transform :number [[_ value]]
  `#{(Integer/parseInt ~value)})

(defmethod transform :symbol [[_ value]]
  `#{(symbol ~value)})

(defmethod transform :string [[_ value]]
  `#{~(string/replace value #"\"" "")})

(defmethod transform :wildcards [[_ value]]
  (-> value keyword wildcards))

(defn pattern->spec [pattern]
  (->> pattern parser first transform eval))

(comment
  (-> "(+ 1 1)"
      pattern->spec
      (s/valid? '(+ 1 1))))