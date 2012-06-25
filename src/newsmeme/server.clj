(ns newsmeme.server
  (:require [noir.server :as server]
            [korma.db :as db])
  (:use [ring.middleware.anti-forgery :only [wrap-anti-forgery]]))

(db/defdb default  (db/postgres {:db "newsmeme"
                                 :user "postgres"
                                 :password ""}))


(server/load-views "src/newsmeme/views/")
(server/add-middleware wrap-anti-forgery)

(defn -main [& m]
  (let [mode (keyword (or (first m) :dev))
        port (Integer. (get (System/getenv) "PORT" "8080"))]
    (server/start port {:mode mode
                        :ns 'newsmeme})))

