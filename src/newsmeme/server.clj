(ns newsmeme.server
  (:require [noir.server :as server]
            [korma.db :as db]))

(db/defdb default  (db/postgres {:db "newsmeme"
                                 :user "postgres"
                                 :password ""}))


(server/load-views "src/newsmeme/views/")

(defn -main [& m]
  (let [mode (keyword (or (first m) :dev))
        port (Integer. (get (System/getenv) "PORT" "8080"))]
    (server/start port {:mode mode
                        :ns 'newsmeme})))

