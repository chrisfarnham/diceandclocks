(ns dice-and-clocks.db)

(def search
  (subs (.. js/window -location -search) 1))

(def pathname
  (subs (.. js/window -location -pathname) 1))

(def default-db
  {:name search :channel pathname})

