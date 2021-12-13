(ns clj-holmes.rules.utils)

(defn is-patterns-declaration? [entry]
  (and (map? entry)
       (or (:patterns entry)
           (:patterns-either entry))))

(defn is-pattern-declaration? [entry]
  (and (map? entry)
       (:pattern entry)))

(defn is-pattern-not-declaration? [entry]
  (and (map? entry)
       (:pattern-not entry)))

(defn is-any-pattern-declaration? [entry]
  (and (map? entry)
       (or (:pattern-not entry)
           (:pattern entry))))