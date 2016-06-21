(ns soundera.yql
  (:require
    [soundera.xhr :as xhr]
    [cemerick.url :refer [url]]))

(defn- make-url [target]
  (let [query (str "select * from json where url=\"" target "\"")]
    (-> (url "http://query.yahooapis.com/v1/public/yql")
      (assoc :query {:q query :format "json"})
      str)))

(defn query [target]
  (let [query-url (make-url target)]
    (xhr/get query-url)))