(ns newsmeme.models
  (:use [korma.core]))

(declare post tag post-comment)

(defentity user
           (pk :id)
           (table :users)
           (has-many post))

(defentity post
           (pk :id)
           (table :posts)
           (belongs-to user)
           (has-many tag))


(defentity tag
           (pk :id)
           (table :tags)
           (has-many post))


(defentity post-comment
           (pk :id)
           (table :comments)
           (belongs-to post)
           (belongs-to user)
           (belongs-to post-comment)
           (has-many post-comment))



