(ns newsmeme.models.posts
  (:require [clojure.string :as string]
            [noir.session :as session])
  (:use [korma.core]
        [newsmeme.models.users :only [user current-user friend-ids]]))

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



(def access-public 100)
(def access-friends 200)
(def access-private 300)

(def access-names {access-public "public"
                   access-friends "friends"
                   access-private "private"})


(defn access-name [access]
  (get access-names access))


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
  [{:keys [title link description access tags]}]
  (let [new-post (insert post 
                         (values {:title title
                                  :link link
                                  :description description
                                  :access (Integer. access)
                                  :author_id 1}))
        tag-ids (tag-ids-from-string tags)]
    (doall (add-tags-to-post (:id new-post) tag-ids)) new-post))


   
(defn restrict
  [q]
  (if-let [user (current-user)]
    (do (println "with user" (:username user))
    (where q (-> (or (= :access access-public)
                     ;(and (= :access access-friends)
                     ;        (in :author_id (friend-ids user)))
                     (= :author_id (:id user))))))
    (where q {:access access-public})))

(def select-posts
  (-> (select* post) (with user) (restrict) (order :date_created :DESC)))
    

(defn get-top-posts 
  [] 
  (-> select-posts (select)))

(defn get-latest-posts 
  []
  (-> select-posts (order :score :DESC) (select)))

(defn get-deadpooled-posts
  []
  (-> select-posts (where (<= :score 0))(select)))

(defn get-posts-for-user
  [user-id]
  (-> select-posts (where {:author_id user-id})(select)))

(defn get-posts-for-tag
  [slug]
  (-> select-posts
          (join tagged (= :posts.id :post_tags.post_id))
          (join tag (= :tags.id :post_tags.tag_id))
          (where {:tags.slug slug}) (select))) 


(defn get-post 
  [post-id]
  (first (-> select-posts (where {:id (Integer. post-id)}) (select))))


