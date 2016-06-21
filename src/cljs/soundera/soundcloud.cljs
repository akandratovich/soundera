(ns soundera.soundcloud
  (:require
    [soundera.yql :as yql]
    [cemerick.url :refer [url]]
    [cljs.core.async :as async :refer [chan close!]])
  (:require-macros
    [cljs.core.async.macros :refer [go]]))

(let [argv (clj->js {:client_id "500e2471780b0738b51f4a6ae51a8a06"})]
  (.initialize js/SC argv))

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

(defn- make-chart-url [genre]
  (-> (url "https://api-v2.soundcloud.com/charts")
    (assoc :query {:kind "top" :genre genre :client_id "500e2471780b0738b51f4a6ae51a8a06" :limit "20"})
    str))

(defn fetch-chart [genre]
  (let [chart-url (make-chart-url genre)
        ch (chan 1)]
    (go (let [chart (<! (yql/query chart-url))
              data (-> chart
                     JSON/parse
                     js->clj
                     (get-in ["query" "results" "json" "collection"]))]
          (>! ch (map select-view data)) (close! ch)))
    ch))

; SC.stream('/tracks/293').then(function(player){
;   player.play();
; });

(defn- create-stream-state-handler [id callback]
  (fn [event]
    (case event
      "error" (callback))))

(defn- create-stream-handler [id callback]
  (fn [player]
    (.on player "state-change" (create-stream-state-handler id callback))
    (.play player)))

(defn play [id callback]
  (.then (.stream js/SC (str "/tracks/" id)) (create-stream-handler id callback)))