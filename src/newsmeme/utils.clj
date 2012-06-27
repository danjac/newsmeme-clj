(ns newsmeme.utils
  (:import [java.net.URL]))


(defn domain 
  "Gets domain from a URL e.g. http://reddit.com -> reddit.com"
  [url]
  (.getHost (new java.net.URL url)))

