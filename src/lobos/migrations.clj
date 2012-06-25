(ns lobos.migrations
  ;; exclude clojure builtins
  (:refer-clojure :exclude [alter drop bigint boolean char double float time])
  (:use (lobos (migration :only [defmigration])
                core
                schema
                config)))

(defmigration add-users-table
              (up [] (create defaultdb
                             (table :users 
                                    (integer :id :primary-key :auto-inc)
                                    (varchar :username 60 :unique :not-null)
                                    (varchar :email 150 :unique :not-null)
                                    (varchar :password 80 :not-null)
                                    (varchar :openid 80 :unique)
                                    (integer :karma (default 0))
                                    (timestamp :date_joined (default (now)))
                                    (varchar :activation_key 80 :unique)
                                    (integer :role (default 100))
                                    (boolean :receive_email (default false))
                                    (boolean :email_alerts (default false))
                                    (text :followers)
                                    (text :following))))
              (down [] (drop (table :users))))

(defmigration add-posts-table
              (up [] (create defaultdb
                             (table :posts
                                    (integer :id :primary-key :auto-inc)
                                    (integer :author_id [:refer :users :id] :not-null)
                                    (varchar :title 200)
                                    (varchar :link 250)
                                    (timestamp :date_created (default (now)))
                                    (integer :score (default 1))
                                    (integer :num_comments (default 0))
                                    (text :votes)
                                    (integer :access (default 100)))))
              (down [] (drop (table :posts))))

(defmigration add-comments-table
              (up [] (create defaultdb
                             (table :comments
                                    (integer :id :primary-key :auto-inc)
                                    (integer :post_id [:refer :posts :id] :not-null)
                                    (integer :author_id [:refer :users :id] :not-null)
                                    (integer :parent_id [:refer :comments :id])
                                    (text :comment)
                                    (timestamp :date_created (default (now)))
                                    (integer :score (default 1))
                                    (text :votes))))
              (down [] (drop (table :comments))))

(defmigration add-tags-table
              (up [] (create defaultdb
                             (table :tags
                                    (integer :id :primary-key :auto-inc)
                                    (varchar :slug 80 :unique :not-null)))
                      (create defaultdb
                             (table :post_tags
                                    (integer :post_id [:refer :posts :id] :not-null)
                                    (integer :tag_id [:refer :tags :id] :not-null))))
              (down [] (drop (table :post_tags))
                       (drop (table :tags))))
