(ns newsmeme.views.common
  (:use [noir.core :only [defpartial]]
        [noir.session :only [flash-get]]
        [hiccup.page-helpers :only [link-to include-css include-js html5]]))


(defpartial show-flash []
            ;; show flash message if available
            (if-let [message (flash-get)]
              [:ul.messages [:li.info message]]))

(defpartial main-nav []
            [:div.navigation.span-24.last
             [:ul.span-24
              [:li.first (link-to "/" "hot")]
              [:li (link-to "/latest/" "new")]
              [:li (link-to "/deadpool/" "deadpool")]
              [:li (link-to "/submit/" "submit")]
              [:li.last [:form#search {:action "/search/" :method "GET"} 
                         [:div [:input {:placeholder "search"}]]]]]])

(defpartial top-nav []
            [:ul.topnav.span-12.last
             [:li (link-to "/login/" "login")]
             [:li (link-to "/signup/" "signup")]
             [:li (link-to "/contact/" "contact us")]
             [:li (link-to "/rules/" "rules")]
             [:li (link-to "#" "report bugs")]])

(defpartial show-header [header]
            [:div.header.span-24.last [:h1.span-12 header]
             (top-nav)])

(defpartial show-footer []
            [:div.footer.span-24.last "&copy; Copyright 2012 Dan Jacob"])

(defpartial show-content [& content]
            [:div.content.span-24.last (show-flash) content])

(defpartial layout [header & content]
            (html5
              [:head
               [:title "newsmeme"]
               (include-css "/css/blueprint/screen.css"
                            ;;"/css/blueprint/print.css"
                            "/css/base.css")
               (include-js "/js/jquery.min.js"
                           "/js/newsmeme.js")
               [:link {:rel "shortcut icon" :href "/images/favicon.ico"}]]
               ;; (include-css "/css/blueprint/ie.css")
              [:body
               [:div.container
                (show-header header)
                (main-nav)
                (show-content content)
                (show-footer)]]))
