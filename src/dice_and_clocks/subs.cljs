(ns dice-and-clocks.subs
  (:require
   [clojure.string :as string]
   [re-frame.core :as re-frame]
   [dice-and-clocks.firebase-database :as db]))

(re-frame/reg-sub
 ::name
 (fn [db]
   (:name db)))

(re-frame/reg-sub
 ::channel
 (fn [db]
   (:channel db)))

(defn channels-path [channel]
  [:channels (keyword channel)])

(defn messages-path [channel]
  (conj (channels-path channel) :messages))

(defn clocks-path [channel]
  (conj (channels-path channel) :clocks))

;; Before a channel is chosen, `channel` is "" and messages-path/clocks-path
;; point at /channels/"" — Firebase correctly denies that read, logging a
;; permission_denied error on every landing-page visit. Only subscribe to
;; the underlying Firebase path once a real channel exists.

(re-frame/reg-sub
 ::messages
 (fn [_]
   (let [channel @(re-frame/subscribe [::channel])]
     (when-not (string/blank? channel)
       @(re-frame/subscribe [::db/realtime-value {:path (messages-path channel)}])))))

(re-frame/reg-sub
 ::clocks
 (fn [_]
   (let [channel @(re-frame/subscribe [::channel])]
     (when-not (string/blank? channel)
       @(re-frame/subscribe [::db/realtime-value {:path (clocks-path channel)}])))))
