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

"Tagging functions"

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
  (map #(insert-tagged post-id %) tag-ids))

(defn tag-ids-from-string
  [tags]
  (let [tags (filter not-empty (map #(string/lower-case 
                                       (string/trim %)) 
                                    (string/split tags #"\s")))]
       (map #(get-or-create-tag %) tags)))

(defn get-tags-for-post
  [post-id]
  (map #(:slug %) 
          (select tag
          (join tagged (= :tags.id :post_tags.tag_id))
          (where {:post_tags.post_id (Integer. post-id)}))))


(defn link-exists?
  "Check link already used by another post"
  [link]
  (and (not (empty? link)) 
       (first (select post (where {:link link})))))


(defn insert-post 
  "Insert a new post in the database" 
  [{:keys [title link description tags]}]
  (let [new-post (insert post 
                         (values {:title title
                                  :link link
                                  :description description
                                  :author_id 1}))
        tag-ids (tag-ids-from-string tags)]
    (doall (add-tags-to-post (:id new-post) tag-ids)) new-post))


(defn restrict
  [q]
  (if-let [user-id (session/get :user-id)]
    (where q (-> (or (= :access 100)
                     (= :author_id user-id))))
    (where q {:access 100})))


(defn get-top-posts 
  [] 
  (select post (with user) (restrict) (order :score :DESC :date_created :DESC)))

(defn get-latest-posts 
  []
  (select post (with user) (restrict) (order :date_created :DESC)))


(defn get-posts-for-tag
  [slug]
  (select post (with user) (restrict) 
          (join tagged (= :posts.id :post_tags.post_id))
          (join tag (= :tags.id :post_tags.tag_id))
          (where {:tags.slug slug}) (order :date_created :DESC)))


(defn get-post 
  [post-id]
  (first (select post (restrict) (where {:id (Integer. post-id)}))))


