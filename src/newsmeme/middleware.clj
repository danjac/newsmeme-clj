(ns newsmeme.middleware
    (:require [noir.session :as session]
              [korma.core :as db]
              [newsmeme.models :as models]))


(declare ^{:dynamic true} *current-user*)


(defn get-user [user-id]
  (first (db/select models/user (db/where {:id user-id}))))


(defn session-user []
    (if-let [user-id (session/get :user-id)]
      (get-user user-id)))


(defn authenticate [handler]
  "Looks up user ID in session, pulls data from DB into session"
    (fn [req]
        (binding [*current-user* (session-user)]
        (let [response (handler req)] response))))


