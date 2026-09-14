(ns dice-and-clocks.firebase-analytics
  (:require [cljs-bean.core :refer [->js ->clj]]
            [dice-and-clocks.config :as config]))

(defn analytics ^js [] (.analytics ^js js/firebase))

(defn log-event [event properties]
  (when (config/track-analytics?)
    (-> (analytics)
        (.logEvent (->js event) (->js properties)))))