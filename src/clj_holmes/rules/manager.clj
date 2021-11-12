(ns clj-holmes.rules.manager
  (:require [clj-holmes.rules.wagon.github :as github]
            [clojure.java.io :as io])
  (:import (java.net URI)))

(defmulti ^:private fetch* (fn [^URI repository _] (keyword (.getScheme repository))))

(defmethod fetch* :git [repository output-directory]
  (println "Fetching rules from github")
  (github/fetch repository output-directory)
  (println "Done"))

(defn fetch [{:keys [output-directory repository]}]
  (let [repository (URI. repository)]
    (.mkdirs (io/file output-directory))
    (fetch* repository output-directory)))

(comment
  (fetch {:output-directory "/tmp/clj-holmes-rules"
          :repository "git://clj-holmes/clj-holmes-rules#main"}))