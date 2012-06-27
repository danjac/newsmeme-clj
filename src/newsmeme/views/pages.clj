(ns newsmeme.views.pages
  (:import [java.net.URL]
           [java.net.MalformedURLException])
  (:require [newsmeme.views.common :as common]
            [newsmeme.models.posts :as posts]
            [newsmeme.models.users :as users]
            [newsmeme.validators :as validators]
            [newsmeme.utils :as utils]
            [noir.response :as resp]
            [noir.session :as session]
            [noir.cookies :as cookies])
  (:use [noir.core]
        [hiccup.core]
        [hiccup.page-helpers]
        [hiccup.form-helpers]))



(pre-route "/submit/" {} (common/login-required))


(defpartial show-post [{:keys [id title link num_comments date_created score username]}]
            [:li [:h3 (link-to {:target "_blank" :class "public"} link title)
                      [:span.domain " &rarr; " (utils/domain link)]]
                 [:p.post-info "Comments " num_comments 
                               " | Score " score 
                               " | Posted today by " (link-to "#" username)]])



(defpage "/" []
         (common/layout
           [:ul.posts 
            (map show-post (posts/get-top-posts))]))


(defpage [:post "/submit/"] {:as post}
         (if (validators/valid-post? post)
           (do (posts/insert-post post)
               (session/flash-put! "Thanks for your post!")
               (resp/redirect "/"))
           (render "/submit/" post)))


(defpage "/submit/" {:as post} 
         (common/layout
           [:h2 "Submit a post"]
           (form-to [:post "/submit/"]
                    (common/csrf-field)
                    [:ul
                     [:li (common/show-errors :title)
                          (label :title "Title")
                          (text-field :title (:title post))]
                     [:li (common/show-errors :link)
                          (label :link "Link")
                          (text-field :link (:link post))]
                     [:li (submit-button "Submit")]])))

             
(defpage [:post "/login/"] {:keys [creds password next-url]}
         (if-let [user (users/auth-user creds password)]
           (do (session/put! :user-id (user :id))
               (session/flash-put! (str "Welcome back, " (user :username)))
               (resp/redirect (or next-url "/")))
           (do (session/flash-put! "Sorry, invalid login")
               (resp/redirect (url "/login/" {:next-url (or next-url "/")})))))


(defpage "/login/" {:keys [next-url]}
         (common/layout
            (form-to [:post "/login/"]
                     (common/csrf-field)
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
         (if (validators/valid-signup? user)
           (do (let [user-id (:id (users/insert-user user))]
                  (session/put! :user-id user-id))
                  (session/flash-put! (str "Welcome to newsmeme, " (user :username)))
                  (resp/redirect "/"))
           (render "/signup/" user)))

(defpage "/signup/" {:as user}
         (common/layout 
            (form-to [:post "/signup/"]
                     (common/csrf-field)
                     [:ul
                      [:li (common/show-errors :username)
                           (label :username "username")
                           (text-field :username (user :username))]
                      [:li (common/show-errors :email) 
                           (label :email "email address")
                           (text-field :email (user :email))]
                      [:li (common/show-errors :password)
                           (label :password "password")
                           (password-field :password)]
                      [:li (common/show-errors :password-again)
                           (label :password-again "password again")
                           (password-field :password-again)]
                      [:li (submit-button "signup")]])))
