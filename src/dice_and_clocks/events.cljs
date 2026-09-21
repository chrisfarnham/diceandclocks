(ns dice-and-clocks.events
  (:require
   [re-frame.core :as re-frame]
   [dice-and-clocks.db :as db]
   [dice-and-clocks.utils :as utils]
   ))

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))

(re-frame/reg-fx
 ::navigate
 (fn [url] (set! (.-location js/window) url)))

(re-frame/reg-event-fx
 :channel-name
 (fn [{:keys [db]} [_ channel-name]]
   (let [{:keys [channel name] :as slugged} (update channel-name :channel utils/slugify)]
     {:db        (merge db slugged)
      ::navigate (str "/" channel "?" name)})))

(re-frame/reg-event-fx
 :channel
 (fn [{:keys [db]} [_ channel]]
   {:db        (assoc db :channel channel)
    ::navigate (str "/" channel)}))

(re-frame/reg-event-fx
 :name
 (fn [{:keys [db]} [_ name]]
   {:db (assoc db :name name)
    ::navigate (str (.. js/window -location -pathname) "?" name)}))

