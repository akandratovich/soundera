(ns soundera.core
  (:require
    [goog.net.XhrIo :as xhr]
    [cljs.core.async :as async :refer [chan close!]])
  (:require-macros
    [cljs.core.async.macros :refer [go alt!]]))

(def music-genre (atom "soundcloud:genres:techno"))

(defn- make-query-url []
  (str "http://query.yahooapis.com/v1/public/yql"
       "?q=select+*+from+json+where+url%3D%22https%3A%2F%2Fapi-v2.soundcloud.com%2Fcharts%3Fkind%3Dtop%26genre%3Dsoundcloud%253Agenres%253Atechno%26client_id%3D500e2471780b0738b51f4a6ae51a8a06%26offset%3D0%26limit%3D20%22"
       "&format=json"))

(defn- GET [url]
  (let [ch (chan 1)]
    (xhr/send
      url
      (fn [event]
        (let [res (-> event .-target .getResponseText)]
          (go (>! ch res) (close! ch)))))
    ch))

(defn- select-view [track]
  (let [score (track "score")
        info (track "track")
        user (info "user")
        track-view (select-keys info ["id" "permalink_url" "title"])
        user-view (select-keys user ["permalink_url" "username"])]
    (assoc track-view
      "user" user-view
      "score" (int score)
      "duration" (int (info "duration")))))

(defn- select-track [data]
  (let [summary (reduce + (map #(% "duration") data))
        current-time (.getTime (js/Date.))
        current-millis (rem current-time (* 24 60 60 1000))
        [__ timeref selected-track] (reduce (fn [[status remaining selected] current]
                                      (let [duration (current "duration")]
                                        (if status
                                          (if (> remaining duration)
                                            [true (- remaining duration) current]
                                            [false remaining current])
                                          [false remaining selected])))
                              [true (rem current-millis summary) nil] data)]
    {:track selected-track :timestamp timeref}))

(defn- rotate [result]
  (let [data (-> result
               JSON/parse
               js->clj
               (get-in ["query" "results" "json" "collection"]))
        chart (map select-view data)
        selected (select-track chart)]
    (.log js/console (clj->js selected))))

(defn- query []
  (let [url (make-query-url)]
    (go (rotate (<! (GET url))))))

(query)
