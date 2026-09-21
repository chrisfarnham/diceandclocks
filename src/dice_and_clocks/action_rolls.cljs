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

;; Text and bold-emphasis spans copied verbatim from the Blades in the
;; Dark SRD, "ACTION ROLL" section (Blades-in-the-Dark-SRD.md, the
;; CONTROLLED/RISKY/DESPERATE subsections spanning lines 398-427):
;;   :critical    <- "Critical" bullet, all three positions (identical text)
;;   :controlled  <- "#### CONTROLLED" bullets: 6 / 4-5 / 1-3 (lines 403-409)
;;   :risky       <- "#### RISKY" bullets: 6 / 4-5 / 1-3 (lines 416-418)
;;   :desperate   <- "#### DESPERATE" bullets: 6 / 4-5 / 1-3 (lines 425-427)
(def descriptions
  {:critical [[:p "Critical: You do it with " [:b "increased effect"] "."]]
   :controlled [[:p "You do it."]
                [:p "You hesitate. Withdraw and try a different approach, or else do it with a"
                 " minor consequence: a " [:b "minor complication"] " occurs, you have "
                 [:b "reduced effect"] ", you suffer " [:b "lesser harm"]
                 ", you end up in a " [:b "risky"] " position."]
                [:p "You falter. Press on by seizing a " [:b "risky"]
                 " opportunity, or withdraw and try a different approach."]]
   :risky [[:p "You do it."]
           [:p "You do it, but there's a consequence: you suffer " [:b "harm"] ", a "
            [:b "complication"] " occurs, you have " [:b "reduced effect"]
            ", you end up in a " [:b "desperate"] " position."]
           [:p "Things go badly. You suffer " [:b "harm"] ", a " [:b "complication"]
            " occurs, you end up in a " [:b "desperate"] " position, you "
            [:b "lose this opportunity"] "."]]
   :desperate [[:p "You do it."]
               [:p "You do it, but there's a consequence: you suffer " [:b "severe harm"]
                ", a " [:b "serious complication"] " occurs, you have " [:b "reduced effect"] "."]
               [:p "It's the worst outcome. You suffer " [:b "severe harm"] ", a "
                [:b "serious complication"] " occurs, you " [:b "lose this opportunity"]
                " for action."]]})

(defn- result-tier
  "Index into a position's description list for a d6 result: 6 -> 0
  (best outcome), 4-5 -> 1, 1-3 -> 2 (worst outcome)."
  [result]
  (condp >= result
    3 2
    5 1
    0))

(defn result-description [result position critical]
  (let [k (if critical :critical (keyword (string/lower-case position)))
        i (if critical 0 (result-tier result))]
    ;; get-in returns nil for an unmatched position/tier, which renders
    ;; nothing -- intentional, not an oversight; every reachable
    ;; position/result combination in this app always matches.
    [:<> (get-in descriptions [k i])]))