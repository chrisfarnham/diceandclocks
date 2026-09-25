(ns dice-and-clocks.action-rolls
  (:require [clojure.string :as string]
            [dice-and-clocks.i18n :as i18n]))

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

;; The SRD result text itself (English and Russian) lives in i18n.cljs's
;; `description` lookup -- see that namespace's `descriptions` table for
;; the exact SRD source lines this is copied from.

(defn- result-tier
  "Index into a position's description list for a d6 result: 6 -> 0
  (best outcome), 4-5 -> 1, 1-3 -> 2 (worst outcome)."
  [result]
  (condp >= result
    3 2
    5 1
    0))

(defn result-description [result position critical locale]
  (let [k (if critical :critical (keyword (string/lower-case position)))
        i (if critical 0 (result-tier result))]
    ;; i18n/description returns nil for an unmatched position/tier,
    ;; which renders nothing -- intentional, not an oversight; every
    ;; reachable position/result combination in this app always matches.
    [:<> (i18n/description locale k i)]))