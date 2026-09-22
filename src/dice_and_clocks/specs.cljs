(ns dice-and-clocks.specs
  "clojure.spec definitions for the shapes this app reads from and writes
  to Firebase, validated at the two boundaries that matter: right before
  a reg-event-fx handler (events.cljs) pushes/updates a value, and right
  when a subscription value comes back from Firebase (subs.cljs). A
  validation failure is logged (visible in the console) but never blocks
  the write/read -- there's nowhere yet to surface it to the end user,
  and display-message's :default fallback already handles an
  unrecognized shape at render time; this is a visibility net, not a
  gate."
  (:require [clojure.spec.alpha :as s]
            [dice-and-clocks.clocks :as clocks]))

;; Firebase push-ids are strings, but cljs-bean's ->clj (firebase_database.cljs's
;; on-value-reaction) turns a {id entity} object's keys into keywords by
;; default, and views.cljs's entry->entity folds that key straight into
;; :id -- so an id read back from Firebase (as opposed to one written
;; fresh) shows up as a keyword, not a string. Accept either.
(defn id? [x] (or (string? x) (keyword? x)))

(defn validate!
  "Logs s/explain-str to the console when `value` doesn't conform to
  `spec`, tagged with `context` (e.g. the Firebase path or a call-site
  name) so a failure is traceable back to where it came from. Always
  returns `value` unchanged, so this can wrap a value inline without
  disturbing the caller's control flow."
  [spec context value]
  (when-not (s/valid? spec value)
    (js/console.error (str "spec failure (" context "):\n" (s/explain-str spec value))))
  value)

;; -- clock --------------------------------------------------------------

;; A clock's :key is a real keyword (e.g. :four-b) when this app builds
;; and writes it, but Firebase RTDB has no keyword type -- reading it
;; back via ->clj round-trips it to a plain string ("four-b"), which is
;; exactly why clocks/get-face and friends already coerce with
;; (keyword key) rather than assuming a keyword. Accept both
;; representations here for the same reason.
(def clock-keys (set (map :key clocks/clocks)))
(def clock-key-names (set (map name clock-keys)))

(s/def :clock/key (s/or :keyword clock-keys :string clock-key-names))
(s/def :clock/creator string?)
(s/def :clock/caption string?)
(s/def :clock/tic nat-int?)
(s/def :clock/order nat-int?)
(s/def :clock/id id?)
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
;; nilable, not just opt-un: roll-dice's proto-dice-roll writes :text
;; (and dice-roll's :position/:effect below) as an explicit nil when
;; unset, rather than omitting the key -- opt-un alone only makes the
;; *key* optional, it doesn't allow a present key to hold nil.
(s/def :message/text (s/nilable string?))
(s/def :message/id id?)
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
(s/def :dice-roll/position (s/nilable string?))
(s/def :dice-roll/effect (s/nilable string?))

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
