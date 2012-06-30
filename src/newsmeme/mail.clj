(ns newsmeme.mail
  (require [newsmeme.utils :as utils])
  (use [postal.core :only [send-message]]
       [hiccup.page-helpers :only [url]]))


(def default-from-address "admin@newsmeme.co.uk")

(defn email-subject
  [subject]
  (str "newsmeme: " subject))

(defn recover-password
  [email username activation-key]

  (let [link (utils/absolute-url (url "/recoverpass/" :activation-key activation-key))
        body (format "Hi %s,
  You forgot your password. Please click here: %s to change
  to a new one" username link)]
        
  (send-message {:from default-from-address
                 :to [email]
                 :subject (email-subject "you forgot your email")
                 :body body})))

