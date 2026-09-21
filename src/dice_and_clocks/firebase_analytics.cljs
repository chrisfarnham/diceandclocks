(ns dice-and-clocks.firebase-analytics
  (:require [cljs-bean.core :refer [->js]]
            [dice-and-clocks.config :as config]
            [dice-and-clocks.firebase-app :as firebase-app]
            ["firebase/analytics" :refer [logEvent]]))

(defn log-event [event properties]
  (when (config/track-analytics?)
    (logEvent firebase-app/analytics (name event) (->js properties))))
