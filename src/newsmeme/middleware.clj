(ns newsmeme.middleware
    (:require [noir.session :as session]
              [korma.core :as db]
              [newsmeme.models.users :as users]))


(defn wrap-auth [handler]
  "Looks up user ID in session, pulls data from DB into session"
    (fn [req]
        (binding [users/*current-user* (users/session-user)]
        (let [response (handler req)] response))))


