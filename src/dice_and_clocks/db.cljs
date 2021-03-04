(ns dice-and-clocks.db)


(defn location->channel []
  (second (re-matches #"/(.+)" (.. js/window -location -pathname))))

(def default-db
  {:name "re-frame" :channel nil})

