(ns dice-and-clocks.db
  (:require [dice-and-clocks.utils :as utils]))

(def search
  (subs (.. js/window -location -search) 1))

; js/decodeURIComponent

(def pathname
  (subs (js/decodeURIComponent (.. js/window -location -pathname)) 1))

(def default-db
  {:name search :channel (utils/slugify pathname)})

