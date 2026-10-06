(ns dice-and-clocks.utils
  (:require [clojure.string :as string]))

(defn slugify [s]
  (-> s
      string/trim
      string/lower-case
      (string/replace #"\s+" "-")
      (string/replace #"[^\w-]" "")))

(defn strip-params
  "The player name is the bare query string (?Nick). Referrers such as
  Facebook append key=value params (?Nick&fbclid=...), so drop every
  &-separated segment containing an = sign."
  [s]
  (->> (string/split s #"&")
       (remove #(string/includes? % "="))
       (string/join "&")))

(def shareable-address (get (string/split (.. js/window -location -href) #"\?") 0))
