(ns newsmeme.utils
  (:import [java.net.URL])
  (:require [clj-time.core :as ct]))


(defn domain 
  "Gets domain from a URL e.g. http://reddit.com -> reddit.com"
  [url]
  (try (.getHost (new java.net.URL url))
      (catch java.net.MalformedURLException e)))

(def periods [[ct/in-years "years"] 
              [ct/in-months "months"]
              [ct/in-days "days"]
              [ct/in-hours "hours"]
              [ct/in-minutes "minutes"]])

(defn timesince 
  "Returns time since present as text e.g. 3 hours ago, 1 day ago"
  [dt]
  (let [iv (ct/interval dt (ct/now))]
    (first 
        (for [[f s] periods 
              :let [result (f iv)] 
              :when (> result 1)] 
          (str result " " s)))))


(defn absolute-url 
  [url] url)
                                

               
        




      
    
  


