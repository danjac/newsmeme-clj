(ns newsmeme.models.posts
  (:require [noir.session :as session])
  (:use [korma.core]
        [newsmeme.models.users :only [user]]))

(declare tag post-comment)

(defentity post
           (pk :id)
           (table :posts)
           (belongs-to user {:fk :author_id})
           (has-many post-comment)
           (has-many tag))

(defentity tag
           (pk :id)
           (table :tags)
           (has-many post))


(defn insert-post 
  "Insert a new post in the database" 
  [{:keys [title link]}]
  (insert post (values {:title title
                        :link link
                        :author_id (session/get :user-id)})))


(defn get-top-posts []
  (select post (with user)(order :date_created :DESC)))


 
