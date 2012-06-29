(ns newsmeme.validators
  (:import [java.net.URL]
           [java.net.MalformedURLException])
  (:require [noir.validation :as vali]
            [newsmeme.models.posts :as posts]
            [newsmeme.models.users :as users]))

(defn- valid-url? 
  "Checks if url is valid"
  [url]
    (if-not url (do (println "ok" true))
      (try (new java.net.URL url)
        (catch java.net.MalformedURLException e))))


(defn valid-post? 
  [{:keys [title link description]}]
  (vali/rule (vali/has-value? title)
             [:title "Title is missing"])
  (vali/rule (valid-url? link)
             [:link "Invalid link"])
  (vali/rule (not (posts/link-exists? link))
             [:link "This link has been posted already"])
  (vali/rule (or link description)
             [:description "Please provide a link or a description"])
  (not (vali/errors?)))


(defn valid-signup? 
  [{:keys [username email password password-again]}] 
  (vali/rule (vali/has-value? username)
             [:username "Username is required"])
  (vali/rule (not (users/username-exists? username))
             [:username "This username is taken"])
  (vali/rule (vali/is-email? email)
             [:email "Not a valid email address"]) 
  (vali/rule (not (users/email-exists? email))
             [:email "This email address is taken"])
  (vali/rule (vali/has-value? password)
             [:password "Password is required"])
  (vali/rule (= password password-again)
             [:password-again "Password does not match"])
  (not (vali/errors?)))




