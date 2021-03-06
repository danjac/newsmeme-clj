(ns newsmeme.views.pages
  (:require [newsmeme.views.common :as common]
            [newsmeme.models.posts :as posts]
            [newsmeme.models.users :as users]
            [newsmeme.validators :as validators]
            [newsmeme.utils :as utils]
            [clj-time.coerce :as coerce]
            [noir.response :as resp]
            [noir.session :as session]
            [noir.cookies :as cookies])
  (:use [noir.core]
        [hiccup.core]
        [hiccup.page-helpers]
        [hiccup.form-helpers]))



(pre-route "/submit/" {} (common/login-required))

(defpartial access-buttons 
            [access]
            
            [:label.checkbox "Public" 
             (radio-button :access (= access posts/access-public) posts/access-public)]
            
            [:label.checkbox "Friends"
              (radio-button :access (= access posts/access-friends) posts/access-friends)]

            [:label.checkbox "Private"
              (radio-button :access (= access posts/access-private) posts/access-private)])


(defpartial post-link
  [post-id title link access]
  (let [attrs {:class (posts/access-name access)}]
    (if (empty? link)
      (link-to attrs (str "/post/" post-id) title)
      (link-to (assoc attrs :target "_blank")  link title))))


(defpartial show-post 
            [{:keys [id title access link num_comments date_created score username]}]
            (let [ts (utils/timesince (coerce/from-sql-date date_created))]
              [:li [:h3 (post-link id title link access)
                    (if-not (empty? link) [:span.domain " &rarr; " (utils/domain link)])]
                   [:p.post-info "Comments " num_comments 
                                 " | Score " score 
                                 " | Posted " (if ts (str ts " ago") "just now")
                                 " by " (link-to "#" username)]]))

(defpartial show-tag 
            [tag]
            [:li (link-to (str "/tag/" tag) tag)])


(defpartial show-posts [posts]
            (if (empty? posts)
              [:p [:strong "Nothing found here"]]
            [:ul.posts
             (map show-post posts)]))


(defpage "/" []
         (common/layout 
           [:h2 "Hottest posts"]
           (show-posts (posts/get-top-posts))))


(defpage "/latest/" []
         (common/layout 
           [:h2 "Latest posts"]
           (show-posts (posts/get-latest-posts))))

(defpage "/deadpool/" []
         (common/layout
           [:h2 "Deadpool"]
           (show-posts (posts/get-deadpooled-posts))))

(defpage "/post/:post-id" {:keys [post-id]}
         (if-let [post (posts/get-post post-id)]
           (let [tags (posts/get-tags-for-post post-id)]
             (common/layout
               [:h2 (:title post)]
               (if tags [:ul.tags (map show-tag tags)])))))


(defpage "/tag/:tag" {:keys [tag]}
         (common/layout
            [:h2 "Posts tagged '" tag "'"]
           (show-posts (posts/get-posts-for-tag tag))))
             

(comment
(defpage "/user/:username" {:keys [username]}
         (if-let [user (get-user-by-username username)]
           [:h2 "Posts by " username]
           (show-posts (posts/get-posts-for-user (:id user)))))
)
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
                     [:li (common/show-errors :tags)
                          (label :tags "Tags")
                          (text-field :tags (:tags post))]
                     [:li (common/show-errors :access)
                          (label :access "Access level")
                          (access-buttons (Integer. (or (:access post) posts/access-public)))]
                     [:li (common/show-errors :description)
                          (label :description "Description")
                          (text-area :description (:description post))]
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


(defpage "/forgotpassdone/" []
         (common/layout
           [:p "Please check your email for a link to change your password"]))


