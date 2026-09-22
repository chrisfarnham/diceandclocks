(ns dice-and-clocks.events
  (:require
   [re-frame.core :as re-frame]
   [dice-and-clocks.db :as db]
   [dice-and-clocks.utils :as utils]
   [dice-and-clocks.subs :as subs]
   [dice-and-clocks.config :as config]
   [dice-and-clocks.firebase-analytics :as analytics]
   [dice-and-clocks.firebase-database :as fdb]
   ))

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))

(re-frame/reg-fx
 ::navigate
 (fn [url] (set! (.-location js/window) url)))

(re-frame/reg-fx
 ::log-event
 (fn [[event properties]] (analytics/log-event event properties)))

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

(defn- clock-event-message [db text clock]
  (merge {:message-type "clock-event" :sender (:name db) :text text} clock))

(re-frame/reg-event-fx
 :mark-message-deleted
 (fn [_ [_ message-path]]
   {::fdb/update-fx {:value {:deleted? true} :path message-path}}))

(re-frame/reg-event-fx
 :restore-clock
 (fn [_ [_ clock-path]]
   {::fdb/update-fx {:path clock-path :value {:deleted? nil}}}))

(re-frame/reg-event-fx
 :mark-clock-deleted
 (fn [{:keys [db]} [_ clock-path caption]]
   {:fx [[::fdb/update-fx {:value {:deleted? true} :path clock-path}]
         [::fdb/push-fx {:path (subs/messages-path (:channel db))
                          :value {:message-type "clock-deleted" :sender (:name db)
                                  :clock-path clock-path :caption caption}}]]}))

(re-frame/reg-event-fx
 :persist-dice-roll
 (fn [{:keys [db]} [_ dice-results]]
   (let [channel (:channel db)]
     {::fdb/push-fx {:path (subs/messages-path channel)
                      :value (merge dice-results {:sender (:name db)
                                                   :message-type "dice-roll"})}
      ::log-event [:roll-dice {:channel-id channel :name (:name db)}]})))

(re-frame/reg-event-fx
 :send-message
 (fn [{:keys [db]} [_ message]]
   (let [channel (:channel db)]
     {::fdb/push-fx {:path (subs/messages-path channel)
                      :value {:message-type "message" :sender (:name db) :text message}}
      ::log-event [:send-message {:channel-id channel :name (:name db)}]})))

(re-frame/reg-event-fx
 :create-clock
 (fn [{:keys [db]} [_ key caption clock-count]]
   (let [channel (:channel db)
         clock {:key key :creator (:name db) :caption caption :tic 0 :order clock-count}]
     {:fx [[::fdb/push-fx {:path (subs/clocks-path channel) :value clock}]
           [::fdb/push-fx {:path (subs/messages-path channel)
                            :value (clock-event-message db "created a new clock" clock)}]]
      ::log-event [:create-clock {:channel-id channel :name (:name db) :caption caption}]})))

(defn- adjust-clock
 [{:keys [db]} clock-path clock delta verb]
 (let [channel (:channel db)
       new-clock (update clock :tic + delta)]
   {:fx [[::fdb/update-fx {:path clock-path :value {:tic (:tic new-clock)}}]
         [::fdb/push-fx {:path (subs/messages-path channel)
                          :value (clock-event-message db verb new-clock)}]]}))

(re-frame/reg-event-fx
 :advance-clock
 (fn [cofx [_ clock-path clock]]
   (adjust-clock cofx clock-path clock 1 "advanced a clock")))

(re-frame/reg-event-fx
 :roll-back-clock
 (fn [cofx [_ clock-path clock]]
   (adjust-clock cofx clock-path clock -1 "rolled back a clock")))

