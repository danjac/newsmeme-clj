(ns newsmeme.views.welcome
  (:require [newsmeme.views.common :as common]
            [newsmeme.models :as models]
            [newsmeme.middleware :as middleware]
            [noir.validation :as vali]
            [noir.response :as resp]
            [noir.session :as session]
            [noir.cookies :as cookies]
            [noir.util.crypt :as crypt]
            [korma.core :as db])
  (:use [noir.core]
        [hiccup.core]
        [hiccup.page-helpers]
        [hiccup.form-helpers]))


(defn username-exists? [username]
  (and username (first (db/select models/user (db/where {:username username})))))

(defn email-exists? [email]
  (and email (first (db/select models/user (db/where {:email email})))))

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
  (db/insert models/user (db/values (assoc 
                                      (select-keys user [:username :email :password]) :password 
                                        (crypt/encrypt (user :password))))))


(defn current-user [] middleware/*current-user*)

(defn get-top-posts []
  (db/select models/post))



(defn auth-user [login password]
  (let [user (first (db/select models/user 
                               (db/where (or (= :username login)
                                             (= :email login)))))] user
    (if (and user (crypt/compare password (user :password))) user)))

    

(defpartial show-errors [field]
            (if-let [errors (vali/get-errors field)]
                     [:ul.errors
                      (map (fn [error] [:li.error error]) errors)]))
                      

(defpartial csrf-field []
            (hidden-field "__anti-forgery-token" (cookies/get "__anti-forgery-token")))


(defpartial show-post [post]
            [:li [:h3 (post :title)]])

(defpage "/" []
         (common/layout "newsmeme"
           [:ul.posts 
            (map show-post (get-top-posts))]))


(defpage [:post "/login/"] {:keys [login password]}
         (if-let [user (auth-user login password)]
           (do
             (session/put! :user-id (user :id))
             (session/flash-put! (str "Welcome back, " (user :username)))
             (resp/redirect "/"))
            (resp/redirect "/login/")))


(defpage "/login/" []
         (common/layout "Login"
                        (form-to [:post "/login/"]
                                 (csrf-field)
                                 [:ul
                                    [:li (label :login "Username or email address")
                                         (text-field :login)]
                                    [:li (label :password "Password")
                                         (password-field :password)]
                                    [:li (submit-button "Login")]])))
                                    


(defpage [:post "/signup/"] {:as user}
         (if (valid-signup? user)
           (do
             (let [user-id ((insert-user user) :id)]
                   (session/put! :user-id user-id))
             (session/flash-put! (str "Welcome to newsmeme, " (user :username)))
             (resp/redirect "/"))
           (render "/signup/" user)))

(defpage "/signup/" {:as user}
         (common/layout "Signup to newsmeme"
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


                          


