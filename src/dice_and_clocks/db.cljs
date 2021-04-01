(ns dice-and-clocks.db
  (:require [dice-and-clocks.utils :as utils]
            [clojure.string :as str]))

(def search
  (subs (js/decodeURIComponent (.. js/window -location -search)) 1))

; js/decodeURIComponent

(def pathname
  (subs (str/lower-case (js/decodeURIComponent (.. js/window -location -pathname))) 1))

(def default-db
  {:name search :channel (utils/slugify pathname)})

