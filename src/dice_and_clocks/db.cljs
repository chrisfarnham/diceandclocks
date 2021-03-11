(ns dice-and-clocks.db
  (:require [clojure.string :as string]))

(def search
  (subs (.. js/window -location -search) 1))

; js/decodeURIComponent

(def pathname
  (subs (js/decodeURIComponent (.. js/window -location -pathname)) 1))

(defn slugify
  [string]
  ((comp #(string/replace % #"\W" "")
    #(string/replace % #"\s+" "-")
         string/lower-case
         string/trim)
   string))

(def default-db
  {:name search :channel (slugify pathname)})

