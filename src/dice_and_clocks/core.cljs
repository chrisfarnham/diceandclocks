(ns dice-and-clocks.core
  (:require
   [reagent.dom :as rdom]
   [re-frame.core :as re-frame]
   [dice-and-clocks.events :as events]
   [dice-and-clocks.views :as views]
   [dice-and-clocks.config :as config]
   ))

(defn dev-setup []
  (when config/debug?
    (println "dev mode")
    (println (.. js/window -location -pathname))
    (println (str "url-path->info: " "test"))))
; swtich to https://github.com/metosin/reitit
; http://russmatney.com/2020-04-23-reitit-router-in-reframe/
; 
; 
; Or router5 just looks perfect
; https://router5.js.org/introduction/core-concepts
; https://github.com/pesterhazy/cljs-spa-example

(defn ^:dev/after-load mount-root []
  (re-frame/clear-subscription-cache!)
  (let [root-el (.getElementById js/document "app")]
    (rdom/unmount-component-at-node root-el)
    (rdom/render [views/main-panel] root-el)))

; dice font
; https://fontawesome.com/icons/dice-one?style=solid
; hand drawn clocks
; https://acegiak.itch.io/ashtonhand-clocks

(defn init []
  (re-frame/dispatch-sync [::events/initialize-db])
  (dev-setup)
  (mount-root))
