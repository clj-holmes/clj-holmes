(ns clj-holmes.config)

(def readers
  "List of custom readers."
  {'nu/time identity
   'datomic.db.DbId identity
   'nu/date identity
   'nu/prototypes-for identity
   'nu/workload-for identity})
