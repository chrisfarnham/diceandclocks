(ns dice-and-clocks.action-rolls
  (:require [clojure.string :as string]))

(def positions ["Controlled" "Risky" "Desperate"])

(def effects ["Great" "Standard" "Limited"])

(def combinations (for [p positions e effects] {:position p :effect e}))


(defn generate-dice-results [size]
  (let [pool-size (if (< size 1) 2 size)
        pool (repeatedly pool-size #(+ 1 (rand-int 6)))
        result (if (< size 1) (apply min pool) (apply max pool))
        ; zero size dice pools cannot result in crits
        critical (and (< 0 size) (< 1 (count (filter #(= 6 %) pool))))]
    {:pool (vec pool) :result result :size size :critical critical}))

(def descriptions
  {
   :critical [[:p [:b "Critical:"] " You do it with " [:b "increased effect."]]]
   :controlled [[:p "You do it."] 
                [:p "You hesitate. Withdraw and try a different approach, or else do it with a minor consequence: a minor " [:b "complication "] " occurs, you have " [:b " reduced effect"]
                 ", you suffer " [:b "lesser harm "] ", you end up in a " [:b " risky "] " position."]
                [:p "You falter. Press on by seizing a " [:b "risky"] 
                 " opportunity, or withdraw and try a different approach."]]
   :risky [[:p "You do it."]
           [:p "You do it, but there's a consequence: you suffer " [:b " harm, "] " a " [:b " complication "]
            " occurs, you have " [:b " reduced effect,"] " you end up in a " [:b " desperate "] " position, you "
            [:b " lose this opportunity."]]
           [:p "Things go badly. You suffer " [:b " harm, "] " a " [:b " complication "] 
            " occurs, you end up in a " [:b " desperate "] " position, you " [:b " lose this opportunity."] ]
           ]
   :desperate [[:p "You do it."]
               [:p "You do it, but there's a consequence: you suffer " [:b " severe harm, "]
                " a " [:b " serious complication "] " occurs, you have " [:b " reduced effect."]]
               [:p "It's the worst outcome. You suffer " [:b " severe harm "] ", a "
                [:b " serious complication "] " occurs, you " [:b " lose this opportunity "]
                " for action."]]
  }
)

(defn result-description [result position critical]
  (let [position (keyword (string/lower-case position))] 
  (println (str "result " result " position " position " critical " critical))
  [:<>
  (cond
    (= true critical) (get (:critical descriptions) 0)
    (= :controlled position) (cond (= result 6)    (get (:controlled descriptions) 0)
                                   (<= 4 result 5) (get (:controlled descriptions) 1)
                                   (<= 1 result 3) (get (:controlled descriptions) 2))
    (= :risky position)(cond (= result 6)    (get (:risky descriptions) 0)
                             (<= 4 result 5) (get (:risky descriptions) 1)
                             (<= 1 result 3) (get (:risky descriptions) 2))
    (= :desperate position)(cond (= result 6) (get (:desperate descriptions) 0)
                                 (<= 4 result 5) (get (:desperate descriptions) 1)
                                 (<= 1 result 3) (get (:desperate descriptions) 2))
    )]
))

; combinations