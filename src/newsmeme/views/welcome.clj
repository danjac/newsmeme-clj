(ns newsmeme.views.welcome
  (:import [java.net.URL]
           [java.net.MalformedURLException])
  (:require [newsmeme.views.common :as common]
            [newsmeme.models :as models]
            [noir.validation :as vali]
            [noir.response :as resp]
            [noir.request :as req]
            [noir.session :as session]
            [noir.cookies :as cookies]
            [noir.util.crypt :as crypt]
            [korma.core :as db])
  (:use [noir.core]
        [hiccup.core]
        [hiccup.page-helpers]
        [hiccup.form-helpers]))


(defn valid-url? 
  "Checks if url is valid"
  [url]
    (try (new java.net.URL url)
      (catch java.net.MalformedURLException e)))


(defn username-exists? [username]
  (and username (first (db/select models/user (db/where {:username username})))))

(defn email-exists? [email]
  (and email (first (db/select models/user (db/where {:email email})))))

(defn valid-post? [{:keys [title link]}]
  (vali/rule (vali/has-value? title)
             [:title "Title is missing"])
  (vali/rule (vali/has-value? link)
             [:link "Link is missing"])
  (vali/rule (valid-url? link)
             [:link "Invalid link"])
  (not (vali/errors?)))

(defn valid-signup? [{:keys [username email password password-again]}] 
  (vali/rule (vali/has-value? username)
             [:username "Username is required"])
  (vali/rule (not (username-exists? username))
             [:username "This username is taken"])
  (vali/rule (vali/is-email? email)
             [:email "Not a valid email address"]) 
  (vali/rule (not (email-exists? email))
             [:email "This email address is taken"])
  (vali/rule (vali/has-value? password)
             [:password "Password is required"])
  (vali/rule (= password password-again)
             [:password-again "Password does not match"])
  (not (vali/errors?)))


(defn insert-user [user]
  (db/insert models/user (db/values (assoc (select-keys user [:username :email :password]) :password 
                                        (crypt/encrypt (user :password))))))



(defn insert-post [post]
  (db/insert models/post (db/values (assoc (select-keys post [:title :link]) :author_id (session/get :user-id)))))


(defn get-top-posts []
  (db/select models/post (db/order :date_created :DESC)))


(defn auth-user [creds password]
  (let [user (first (db/select models/user 
                               (db/where (or (= :username creds)
                                             (= :email creds)))))] user
    (if (and user (crypt/compare password (user :password))) user)))

    
(defn login-required []
  (when-not (common/current-user)
    (resp/redirect (url "/login/" {:next-url (:uri (req/ring-request))}))))


(pre-route "/submit/" {} (login-required))


(defpartial show-errors [field]
            (if-let [errors (vali/get-errors field)]
                     [:ul.errors
                      (map (fn [error] [:li.error error]) errors)]))
                      

(defpartial csrf-field []
            (hidden-field "__anti-forgery-token" (cookies/get "__anti-forgery-token")))


(defpartial show-post [{:keys [id title link]}]
            [:li [:h3 (link-to {:target "_blank"} link title)]])



(defpage "/" []
         (common/layout
           [:ul.posts 
            (map show-post (get-top-posts))]))


(defpage [:post "/submit/"] {:as post}
         (if (valid-post? post)
           (do (insert-post post)
               (session/flash-put! "Thanks for your post!")
               (resp/redirect "/"))
           (render "/submit/" post)))


(defpage "/submit/" {:as post} 
         (common/layout
           [:h2 "Submit a post"]
           (form-to [:post "/submit/"]
                    (csrf-field)
                    [:ul
                     [:li (show-errors :title)
                          (label :title "Title")
                          (text-field :title (:title post))]
                     [:li (show-errors :link)
                          (label :link "Link")
                          (text-field :link (:link post))]
                     [:li (submit-button "Submit")]])))

             
(defpage [:post "/login/"] {:keys [creds password next-url]}
         (if-let [user (auth-user creds password)]
           (do (session/put! :user-id (user :id))
               (session/flash-put! (str "Welcome back, " (user :username)))
               (resp/redirect (or next-url "/")))
           (do (session/flash-put! "Sorry, invalid login")
               (resp/redirect (url "/login/" {:next-url (or next-url "/")})))))


(defpage "/login/" {:keys [next-url]}
         (common/layout
            (form-to [:post "/login/"]
                     (csrf-field)
                     (if next-url (hidden-field :next-url next-url))
                     [:ul
                        [:li (label :creds "Username or email address")
                             (text-field :creds)]
                        [:li (label :password "Password")
                             (password-field :password)]
                        [:li (submit-button "Login")]])))
                        


(defpage "/logout/" []
         (session/clear!)
         (session/flash-put! "Bye!")
         (resp/redirect "/"))


(defpage [:post "/signup/"] {:as user}
         (if (valid-signup? user)
           (do
             (let [user-id ((insert-user user) :id)]
                   (session/put! :user-id user-id))
             (session/flash-put! (str "Welcome to newsmeme, " (user :username)))
             (resp/redirect "/"))
           (render "/signup/" user)))

(defpage "/signup/" {:as user}
         (common/layout 
            (form-to [:post "/signup/"]
                     (csrf-field)
                     [:ul
                      [:li (show-errors :username)
                           (label :username "username")
                           (text-field :username (user :username))]
                      [:li (show-errors :email) 
                           (label :email "email address")
                           (text-field :email (user :email))]
                      [:li (show-errors :password)
                           (label :password "password")
                           (password-field :password)]
                      [:li (show-errors :password-again)
                           (label :password-again "password again")
                           (password-field :password-again)]
                      [:li (submit-button "signup")]])))


                          


