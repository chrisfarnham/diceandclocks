(ns dice-and-clocks.utils
  (:require [clojure.string :as string]))

(defn slugify
  [string]
  ((comp #(string/replace % #"\W" "")
         #(string/replace % #"\s+" "-")
         string/lower-case
         string/trim)
   string))