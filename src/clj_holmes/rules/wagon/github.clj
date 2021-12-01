(ns clj-holmes.rules.wagon.github
  (:require [clj-http.lite.client :as client]
            [clojure.java.io :refer [output-stream]]
            [clojure.java.io :as io]
            [clojure.java.shell :as shell])
  (:import (java.io File)
           (java.net URI)))

(defn ^:private tar-decompress [filename output-directory]
  (shell/sh "tar" "xf" filename "-C" output-directory))

(defn ^:private download-tarball [{:keys [owner project branch]}]
  (let [token (System/getenv "GITHUB_TOKEN")
        url (format "https://api.github.com/repos/%s%s/tarball/%s" owner project branch)]
    (loop [opts {:headers          {"Authorization" (str "token " token)}
                 :throw-exceptions false
                 :as               :stream}]
      (let [{:keys [status body]} (client/get url opts)]
        (if (= status 200)
          body
          (recur (dissoc opts :headers :throw-exceptions)))))))

(defn ^:private extract-github-information-from-uri [^URI uri]
  {:owner   (.getAuthority uri)
   :project (.getRawPath uri)
   :branch  (or (.getFragment uri) "master")})

(defn fetch [^URI uri output-directory]
  (let [project-information (extract-github-information-from-uri uri)
        tarball (download-tarball project-information)
        tarball-filename (format "%s/rules.tar" output-directory)]
    (io/copy tarball (File. tarball-filename))
    (tar-decompress tarball-filename output-directory)
    (io/delete-file tarball-filename)))

(comment
  (fetch (URI. "git://clj-holmes/clj-holmes-rules#main") {:output-directory "/tmp/clj-holmes-rules"}))