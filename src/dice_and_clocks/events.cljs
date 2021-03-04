(ns dice-and-clocks.events
  (:require
   [re-frame.core :as re-frame]
   [dice-and-clocks.db :as db]
   ))

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))

(re-frame/reg-event-db
 :channel-name
 (fn [db [_ channel-name]]
   (merge db channel-name)
   (set! (.-location js/window) (str "/" (:channel channel-name) "?" (:name channel-name)))))


(re-frame/reg-event-db
 :channel
 (fn [db [_ channel]]
   (assoc db :channel channel)
   (set! (.-location js/window) (str "/" channel))
   ))

(re-frame/reg-event-db
 :name
 (fn [db [_ name]]
   (assoc db :name name)
   (set! (.-location.search js/window) name)
   ))

