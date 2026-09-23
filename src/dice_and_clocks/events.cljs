(ns dice-and-clocks.events
  (:require
   [re-frame.core :as re-frame]
   [dice-and-clocks.db :as db]
   [dice-and-clocks.utils :as utils]
   [dice-and-clocks.subs :as subs]
   [dice-and-clocks.config :as config]
   [dice-and-clocks.firebase-analytics :as analytics]
   [dice-and-clocks.firebase-database :as fdb]
   [dice-and-clocks.specs :as specs]
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
   (let [message {:message-type "clock-deleted" :sender (:name db)
                   :clock-path clock-path :caption caption}]
     {:fx [[::fdb/update-fx {:value {:deleted? true} :path clock-path}]
           [::fdb/push-fx {:path (subs/messages-path (:channel db))
                            :value (specs/validate! ::specs/message :mark-clock-deleted message)}]]})))

(re-frame/reg-event-fx
 :persist-dice-roll
 (fn [{:keys [db]} [_ dice-results]]
   (let [channel (:channel db)
         message (merge dice-results {:sender (:name db) :message-type "dice-roll"})]
     {::fdb/push-fx {:path (subs/messages-path channel)
                      :value (specs/validate! ::specs/message :persist-dice-roll message)}
      ::log-event [:roll-dice {:channel_id channel :name (:name db)}]})))

(re-frame/reg-event-fx
 :send-message
 (fn [{:keys [db]} [_ message]]
   (let [channel (:channel db)
         message {:message-type "message" :sender (:name db) :text message}]
     {::fdb/push-fx {:path (subs/messages-path channel)
                      :value (specs/validate! ::specs/message :send-message message)}
      ::log-event [:send-message {:channel_id channel :name (:name db)}]})))

(re-frame/reg-event-fx
 :create-clock
 (fn [{:keys [db]} [_ key caption clock-count]]
   (let [channel (:channel db)
         clock (specs/validate! ::specs/clock :create-clock
                                 {:key key :creator (:name db) :caption caption
                                  :tic 0 :order clock-count})
         message (specs/validate! ::specs/message :create-clock
                                   (clock-event-message db "created a new clock" clock))]
     {:fx [[::fdb/push-fx {:path (subs/clocks-path channel) :value clock}]
           [::fdb/push-fx {:path (subs/messages-path channel) :value message}]]
      ::log-event [:create-clock {:channel_id channel :name (:name db) :caption caption}]})))

(defn- adjust-clock
 [{:keys [db]} clock-path clock delta verb]
 (let [channel (:channel db)
       new-clock (update clock :tic + delta)
       message (specs/validate! ::specs/message :adjust-clock
                                 (clock-event-message db verb new-clock))]
   {:fx [[::fdb/update-fx {:path clock-path :value {:tic (:tic new-clock)}}]
         [::fdb/push-fx {:path (subs/messages-path channel) :value message}]]}))

(re-frame/reg-event-fx
 :toggle-theme
 (fn [{:keys [db]} [_ current-theme]]
   (let [channel (:channel db)
         new-theme (get subs/other-theme current-theme subs/default-theme)
         message {:message-type "message" :sender (:name db)
                   :text (str "switched the color scheme to " (get subs/theme-label new-theme))}]
     {:fx [[::fdb/set-fx {:path (subs/theme-path channel) :value new-theme}]
           [::fdb/push-fx {:path (subs/messages-path channel)
                            :value (specs/validate! ::specs/message :toggle-theme message)}]]
      ::log-event [:toggle-theme {:channel_id channel :name (:name db) :theme new-theme}]})))

(re-frame/reg-event-fx
 :advance-clock
 (fn [cofx [_ clock-path clock]]
   (adjust-clock cofx clock-path clock 1 "advanced a clock")))

(re-frame/reg-event-fx
 :roll-back-clock
 (fn [cofx [_ clock-path clock]]
   (adjust-clock cofx clock-path clock -1 "rolled back a clock")))

