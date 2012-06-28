(ns newsmeme.models.posts
  (:require [clojure.string :as string]
            [noir.session :as session])
  (:use [korma.core]
        [newsmeme.models.users :only [user]]))

(declare tag post-comment)

(defentity post
           (pk :id)
           (table :posts)
           (belongs-to user {:fk :author_id})
           (has-many post-comment))

(defentity tag
           (pk :id)
           (table :tags))


(defentity tagged
           (table :post_tags)
           (belongs-to post)
           (belongs-to tag))

(defn link-exists?
  "Check link already used by another post"
  [link]
  (and link 
       (first (select post (where {:link link})))))
  
(defn get-or-create-tag 
  [slug]
  (if-let [result (first (select tag (where {:slug slug})))]
    (:id result)
    (:id (insert tag (values {:slug slug})))))

(defn insert-tagged
  [post-id tag-id]
  (insert tagged (values {:post_id post-id :tag_id tag-id})))

(defn add-tags-to-post 
  [post-id tag-ids]
  ; delete all tags for this post
  (delete tagged (where {:post_id post-id}))
  (map #(insert-tagged post-id %1) tag-ids))

(defn tag-ids-from-string
  [tags]
  (let [tags (map #(string/trim %1) (string/split tags #"\s"))]
       (map #(get-or-create-tag %1) tags)))

(defn insert-post 
  "Insert a new post in the database" 
  [{:keys [title link description tags]}]
  (let [new-post (insert post 
                         (values {:title title
                                  :link link
                                  :description description
                                  :author_id (session/get :user-id)}))]
    (add-tags-to-post (:id new-post) (tag-ids-from-string tags)) new-post))

(defn get-top-posts []
  (select post (with user)(order :date_created :DESC)))


 
