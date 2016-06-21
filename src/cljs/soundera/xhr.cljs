(ns soundera.xhr
  (:require
    [goog.net.XhrIo :as xhr]
    [cljs.core.async :as async :refer [chan close!]])
  (:require-macros
    [cljs.core.async.macros :refer [go]]))

(defn get [url]
  (let [ch (chan 1)]
    (xhr/send
      url
      (fn [event]
        (let [res (-> event .-target .getResponseText)]
          (go (>! ch res) (close! ch)))))
    ch))
