(ns newsmeme.models.users
  (:require [noir.session :as session]
            [clojure.string :as string]
            [noir.util.crypt :as crypt])
  (:use [korma.core]
        [clojure.set :only [intersection]]))

(declare ^{:dynamic true} *current-user*)

(declare post)

(defentity user
           (pk :id)
           (table :users)
           (has-many post))


(defn get-user [user-id]
  (first (select user (where {:id user-id}))))

(defn get-user-from-email [email]
  (first (select user (where {:email email}))))


(defn session-user []
    (if-let [user-id (session/get :user-id)]
      (get-user user-id)))


(defn current-user [] *current-user*)

(defn username-exists? 
  "Checks if a user with this username exists in the db"
  [username]
  (and username (first (select user (where {:username username})))))


(defn email-exists? 
  "Checks if a user with this email exists in the db"
  [email]
  (and email (first (select user (where {:email email})))))


(defn reset-activation-key [user-id]
  "123456")

(defn insert-user 
  "Adds a new user to the db"
  [{:keys [username email password]}]
  (insert user (values {:username username
                        :email email
                        :password (crypt/encrypt password)})))


(defn auth-user 
  "Returns user with matching username or email and password"
  [creds password]
  (let [authd-user (first (select user 
                               (where (or (= :username creds)
                                          (= :email creds)))))] authd-user
    (if (and authd-user (crypt/compare password (authd-user :password))) authd-user)))

(defn friend-ids
  [{:keys [following followers]}]
  (let [following (string/join #"\s" following)
        followers (string/join #"\s" followers)]
    (intersection following followers)))

(defn get-user-by-username
  [username]
  (first (select user (where {:username username}))))


