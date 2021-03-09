(ns cljs.user
  "Commonly used symbols for easy access in the ClojureScript REPL during
  development."
  (:require
   [cljs.repl :refer (Error->map apropos dir doc error->str ex-str ex-triage
                                 find-doc print-doc pst source)]
   [clojure.pprint :refer (pprint)]
   [clojure.string :as str]
   [shadow.resource :as rc]))


(def message [:-MVCos5aJx86-FvVlfxr {:critical false, :effect "", :message-type "dice-roll", :pool [4 4], :position "", :result 4, :sender "christopher", :size 0, :text ""}])

(def a-message (let [[id message] message] {:id id :message message}))


; (type (:id a-message))
; (type (:message a-message))
; a-message
; (assoc nil :key1 4)
; (assoc message :test "test")
; (second message)
; (update a-message :id 'test')