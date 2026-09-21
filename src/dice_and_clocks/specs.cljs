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

;; A 1-die pool is a normal, common roll -- Blades-in-the-Dark-SRD.md
;; line 57: "You'll usually end up with one to four dice. Even one die
;; is pretty good in this game." The 2-die floor only kicks in at the
;; zero-or-negative-dice boundary (SRD line 53: "If you ever need to
;; roll but you have zero (or negative) dice, roll two dice and take
;; the single lowest result. You can't roll a critical when you have
;; zero dice."), which generate-dice-results (action_rolls.cljs)
;; implements by forcing pool-size to 2 whenever size < 1. So the real
;; floor on the stored :pool array is 1, not 2 -- min-count 2 here
;; would incorrectly reject an ordinary 1-die roll.
(s/def :dice-roll/pool (s/coll-of (s/int-in 1 7) :min-count 1))
(s/def :dice-roll/result (s/int-in 1 7))
;; The SRD's own action-rating range is 0-4 (line 97), plus up to two
;; bonus dice normally available (assistance + push/devil's bargain,
;; line 344) -- 6 by the book. This app's UI clamps :size to [0, 9]
;; (views.cljs's increment/decrement, `(min 9 ...)`/`(max 0 ...)`)
;; instead of hard-coding the book's 6, presumably to leave room for
;; GM-granted bonus dice beyond the normal two. 9 is what a client of
;; THIS app can actually produce, which is what this spec checks --
;; not a claim about Blades in the Dark's general rules.
(s/def :dice-roll/size (s/int-in 0 10))
(s/def :dice-roll/critical boolean?)
(s/def :dice-roll/position string?)
(s/def :dice-roll/effect string?)

(defmethod message-type "message" [_]
  (s/keys :req-un [:message/message-type :message/sender :message/text]
          :opt-un [:message/deleted?]))

(defmethod message-type "dice-roll" [_]
  (s/keys :req-un [:message/message-type :message/sender
                    :dice-roll/pool :dice-roll/result :dice-roll/size :dice-roll/critical]
          :opt-un [:message/text :dice-roll/position :dice-roll/effect :message/deleted?]))

(defmethod message-type "clock-event" [_]
  ;; :id is present on "advanced/rolled back a clock" events (merged in
  ;; from the clock's own already-fetched :id) but absent on "created a
  ;; new clock" events (built from a clock map that has no :id yet,
  ;; pre-Firebase-write) -- confirmed against 690 sampled clock-event
  ;; messages: 530 had :id, 160 didn't, matching exactly the
  ;; advance/roll-back vs. create-clock split in views.cljs.
  (s/keys :req-un [:message/message-type :message/sender :message/text
                    :clock/key :clock/tic :clock/caption]
          :opt-un [:message/id :message/deleted?]))

(defmethod message-type "clock-deleted" [_]
  (s/keys :req-un [:message/message-type :message/sender :clock/caption]
          :opt-un [:message/text :message/deleted?]))

(s/def ::message (s/multi-spec message-type :message-type))
