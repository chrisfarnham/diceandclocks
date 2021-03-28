(ns dice-and-clocks.utils
  (:require [clojure.string :as string]))

(defn slugify
  [string]
  ((comp #(string/replace % #"[^\w-]" "")
         #(string/replace % #"\s+" "-")
         string/lower-case
         string/trim)
   string))

(def shareable-address (get (string/split (.-location.href js/window) #"\?") 0))
