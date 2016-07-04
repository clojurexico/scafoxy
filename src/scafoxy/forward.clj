(ns scafoxy.forward
  (:require [org.httpkit.client :as http]
            [ring.util.request :as req]))

(defn- fromhttpkit
  [method-name]
  (ns-resolve 'org.httpkit.client
              (-> method-name
                  name
                  symbol)))


(def full-path
  "Extract full path without the host part,
  so http://example.com/a/b?x=1 would return /a/b?x=1"
  (comp #(nth % 1)
     #(re-find #"\w(/.+)$" %)))


(defn reject-keys
  "Same as clojure.core/select-keys, but
  blacklisting the arguments"
  [original blacklist]
  (let [sblacklist (set blacklist)]
    (reduce-kv (fn [acc key val]
                 (if (contains? sblacklist key)
                   acc
                   (assoc acc key val)))
               {}
               original)))


(defn str-keys
  "Receive a map and return it converting
  every keyword key to string"
  [original]
  (reduce-kv (fn [acc key val]
               (assoc acc
                      (name key) val))
             {}
             original))


(defn request-options
  "From a request, create the appropriate
  options map as required by httpkit"
  [request]
  (let [{headers :headers} request]
    {:headers (reject-keys headers ["host"])}))


(defn adapt-response
  "Adapt the response given by httpkit
  to something that works with ring"
  [response]
  (assoc
   (select-keys response [:body :status])
   :headers
   (-> response :headers str-keys)))


(defn forward
  "Given a request map, forward it
  to a given server and return the given
  response"
  [server-address request]
  (let [{method :request-method} request
        url (-> request
                req/request-url
                full-path)
        httpfn (fromhttpkit method)
        response @(httpfn (str server-address url)
                          (request-options request))]
    (adapt-response response)))
