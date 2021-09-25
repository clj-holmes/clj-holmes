(ns clj-holmes.engine
  (:require [clj-holmes.diplomat.parser :as p]
            [clj-holmes.logic.namespace :as logic.namespace]))

(defn ^:private parser [filename]
  (let [forms (p/code->data! filename)
        ns-declaration (logic.namespace/find-ns-declaration forms)
        forms-without-ns (or (some-> ns-declaration
                                     meta
                                     :index
                                     inc
                                     (drop forms))
                             forms)]
    {:forms forms-without-ns
     :filename filename
     :ns-declaration ns-declaration}))

(comment
  (parser "/home/dpr/dev/nu/machete/project-dependencies/src/project_dependencies/entrypoint.clj"))