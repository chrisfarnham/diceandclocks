(ns dice-and-clocks.firebase-analytics
  (:require [cljs-bean.core :refer [->js]]
            [dice-and-clocks.config :as config]))

(defn analytics ^js [] (.analytics ^js js/firebase))

(defn log-event [event properties]
  (when (config/track-analytics?)
    (-> (analytics)
        (.logEvent (name event) (->js properties)))))