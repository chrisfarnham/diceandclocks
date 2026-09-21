(ns dice-and-clocks.utils
  (:require [clojure.string :as string]))

(defn slugify [s]
  (-> s
      string/trim
      string/lower-case
      (string/replace #"\s+" "-")
      (string/replace #"[^\w-]" "")))

(def shareable-address (get (string/split (.. js/window -location -href) #"\?") 0))
