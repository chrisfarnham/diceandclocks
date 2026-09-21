(ns dice-and-clocks.specs
  "clojure.spec definitions for the shapes this app reads from and writes
  to Firebase. Not yet wired into any dispatch/subscription boundary --
  see PLAN-code-review-findings.md section 3, which calls for that once
  section 2's reg-event-fx consolidation gives these specs a small,
  fixed set of call sites to validate at, instead of the current six
  scattered inline dispatches."
  (:require [clojure.spec.alpha :as s]
            [dice-and-clocks.clocks :as clocks]))

;; -- clock --------------------------------------------------------------

(def clock-keys (set (map :key clocks/clocks)))

(s/def :clock/key clock-keys)
(s/def :clock/creator string?)
(s/def :clock/caption string?)
(s/def :clock/tic nat-int?)
(s/def :clock/order nat-int?)
(s/def :clock/id string?)
(s/def :clock/deleted? boolean?)

(s/def ::clock
  (s/keys :req-un [:clock/key :clock/creator :clock/caption :clock/tic :clock/order]
          :opt-un [:clock/id :clock/deleted?]))

;; -- message ------------------------------------------------------------
;;
;; A multi-spec keyed on :message-type, mirroring display-message's own
;; defmulti dispatch in views.cljs -- one spec per message-type, exactly
;; the four values display-message's defmethods handle.

(s/def :message/message-type #{"message" "dice-roll" "clock-event" "clock-deleted"})
(s/def :message/sender string?)
(s/def :message/text string?)
(s/def :message/id string?)
(s/def :message/deleted? boolean?)

(defmulti message-type :message-type)

;; generate-dice-results (action_rolls.cljs) forces pool-size to 2 when
;; :size is 0 (rolling at disadvantage: take the worse of two dice), so
;; the pool is never fewer than 2 even though :size itself can be 0.
(s/def :dice-roll/pool (s/coll-of (s/int-in 1 7) :min-count 2))
(s/def :dice-roll/result (s/int-in 1 7))
(s/def :dice-roll/size nat-int?)
(s/def :dice-roll/critical boolean?)
(s/def :dice-roll/position string?)
(s/def :dice-roll/effect string?)

(defmethod message-type "message" [_]
  (s/keys :req-un [:message/message-type :message/sender :message/text]))

(defmethod message-type "dice-roll" [_]
  (s/keys :req-un [:message/message-type :message/sender
                    :dice-roll/pool :dice-roll/result :dice-roll/size :dice-roll/critical]
          :opt-un [:message/text :dice-roll/position :dice-roll/effect]))

(defmethod message-type "clock-event" [_]
  (s/keys :req-un [:message/message-type :message/sender :message/text
                    :clock/key :clock/tic :clock/caption]))

(defmethod message-type "clock-deleted" [_]
  (s/keys :req-un [:message/message-type :message/sender :clock/caption]
          :opt-un [:message/text]))

(s/def ::message (s/multi-spec message-type :message-type))
