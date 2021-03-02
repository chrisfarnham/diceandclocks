(ns dice-and-clocks.events
  (:require
   [re-frame.core :as re-frame]
   [dice-and-clocks.db :as db]
   ))

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))
