(ns scafoxy.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [scafoxy.forward :as forward]))

(defroutes app-routes
  (GET "/specific" request
       "this shall not pass")
  (ANY "*" request
       (forward/forward "http://jsonplaceholder.typicode.com" request))
  (route/not-found "Not Found"))


(def app
  (wrap-defaults app-routes site-defaults))
