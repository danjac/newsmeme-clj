(ns newsmeme.views.welcome
  (:require [newsmeme.views.common :as common]
            [newsmeme.models.users :as users]
            [korma.core :as db])
  (:use [noir.core]
        [hiccup.core]
        [hiccup.page-helpers]))


(defn get-users []
  (db/select users/users
             (db/fields :username :email)))

(defpartial show-user [{:keys [username email]}] 
            [:div username " (" 
                  (link-to (str "mailto:" email) email) ")"])


(defpage "/" []
         (common/layout
           [:p "Welcome to newsmeme"]
           (map show-user (get-users))))
