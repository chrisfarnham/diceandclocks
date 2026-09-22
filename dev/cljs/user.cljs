(ns cljs.user
  "Commonly used symbols for easy access in the ClojureScript REPL during
  development."
  (:require
   [cljs.repl :refer (Error->map apropos dir doc error->str ex-str ex-triage
                                 find-doc print-doc pst source)]
   [clojure.pprint :refer (pprint)]
   [clojure.string :as str]
   [shadow.resource :as rc]))


;; A realistic Firebase entry, [id entity], for poking at
;; dice-and-clocks.views/entry->entity et al at the REPL.
(def message [:-MVCos5aJx86-FvVlfxr {:critical false, :effect "", :message-type "dice-roll", :pool [4 4], :position "", :result 4, :sender "christopher", :size 0, :text ""}])

; (assoc nil :key1 4)
; (assoc (second message) :test "test")