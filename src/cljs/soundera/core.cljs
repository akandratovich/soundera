(ns soundera.core
  (:require
    [soundera.soundcloud :as sc]
    [cljs.core.async :as async :refer [chan close!]])
  (:require-macros
    [cljs.core.async.macros :refer [go]]))

(def music-genre (atom "soundcloud:genres:techno"))

(def play-queue (atom []))

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

(defn- update-queue []
  (let [ch (chan 1)]
    (go (let [chart (<! (sc/fetch-chart @music-genre))]
          (reset! play-queue chart)
          (>! ch chart) (close! ch)))
    ch))

(defn- filter-queue [id]
  (let [ch (chan 1)]
    (go (swap! play-queue
          (fn [queue] (filter #(not= id (get-in % ["track" :id])) queue)))
      (>! ch @play-queue) (close! ch))
    ch))

(defn- play-music []
  (go (let [chart (<! (sc/fetch-chart @music-genre))
            selected-track (select-track chart)]
        ; (.log js/console (clj->js selected-track))
        (sc/play (get-in selected-track [:track "id"]) #(.log js/console %))
        ; (sc/play 268289286)
        )))

(play-music)
