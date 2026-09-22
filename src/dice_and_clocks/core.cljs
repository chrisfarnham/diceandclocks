(ns dice-and-clocks.core
  (:require
   [reagent.dom.client :as rdom]
   [re-frame.core :as re-frame]
   [dice-and-clocks.events :as events]
   [dice-and-clocks.views :as views]
   [dice-and-clocks.config :as config]
   [dice-and-clocks.db :as db])
  )

(defn dev-setup []
  (when config/debug?
    (println "dev mode")))

(defonce react-root
  (rdom/create-root (.getElementById js/document "app")))

(defn ^:dev/after-load mount-root []
  (re-frame/clear-subscription-cache!)
  (rdom/render react-root [views/main-panel]))

(defn init []
  (re-frame/dispatch-sync [::events/initialize-db])
  (dev-setup)
  (mount-root))
