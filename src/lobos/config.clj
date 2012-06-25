(ns lobos.config
  (:use lobos.connectivity))

(def defaultdb {:classname "org.postgresql.Driver"
                :subprotocol "postgresql"
                :subname "newsmeme"
                :user "postgres"
                :password ""})

(open-global defaultdb)


