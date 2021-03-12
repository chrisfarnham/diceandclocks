(ns dice-and-clocks.core
  (:require
   [reagent.dom :as rdom]
   [re-frame.core :as re-frame]
   [dice-and-clocks.events :as events]
   [dice-and-clocks.views :as views]
   [dice-and-clocks.config :as config]
   [dice-and-clocks.db :as db])
  )

(defn dev-setup []
  (when config/debug?
    (println "dev mode")
    (println (str " location " db/pathname " and " db/search " and db is " db/default-db))))

(defn ^:dev/after-load mount-root []
  (re-frame/clear-subscription-cache!)
  (let [root-el (.getElementById js/document "app")]
    (rdom/unmount-component-at-node root-el)
    (rdom/render [views/main-panel] root-el)))

(defn init []
  (re-frame/dispatch-sync [::events/initialize-db])
  (dev-setup)
  (mount-root))
