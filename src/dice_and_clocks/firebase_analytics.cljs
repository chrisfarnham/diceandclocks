(ns dice-and-clocks.firebase-analytics
  (:require [cljs-bean.core :refer [->js ->clj]]))

(defn analytics ^js [] (.analytics ^js js/firebase))

(defn log-event [event properties]
  (-> (analytics)
      (.logEvent (->js event) (->js properties))))