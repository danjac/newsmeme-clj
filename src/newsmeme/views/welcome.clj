(ns newsmeme.views.welcome
  (:require [newsmeme.views.common :as common]
            [newsmeme.models :as models]
            [korma.core :as db])
  (:use [noir.core]
        [hiccup.core]
        [hiccup.page-helpers]))


(defn get-top-posts []
  (db/select models/post))

(defpartial show-post [post]
            [:li [:h3 (post :title)]])

(defpage "/" []
         (common/layout "newsmeme"
           [:ul.posts 
            (map show-post (get-top-posts))]))
