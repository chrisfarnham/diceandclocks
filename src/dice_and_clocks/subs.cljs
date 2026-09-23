(ns dice-and-clocks.subs
  (:require
   [clojure.string :as string]
   [re-frame.core :as re-frame]
   [dice-and-clocks.firebase-database :as db]
   [dice-and-clocks.specs :as specs]))

(re-frame/reg-sub
 ::name
 (fn [db]
   (:name db)))

(re-frame/reg-sub
 ::channel
 (fn [db]
   (:channel db)))

(defn channels-path [channel]
  [:channels (keyword channel)])

(defn messages-path [channel]
  (conj (channels-path channel) :messages))

(defn clocks-path [channel]
  (conj (channels-path channel) :clocks))

(defn theme-path [channel]
  (conj (channels-path channel) :theme))

(def default-theme "blades")

(def other-theme {"blades" "blades68" "blades68" "blades"})

(def theme-label {"blades" "Blades in the Dark" "blades68" "Blades '68"})

(re-frame/reg-sub
 ::theme
 (fn [_]
   (let [channel @(re-frame/subscribe [::channel])]
     (if (string/blank? channel)
       default-theme
       (or @(re-frame/subscribe [::db/realtime-value {:path (theme-path channel)}])
           default-theme)))))

;; Before a channel is chosen, `channel` is "" and messages-path/clocks-path
;; point at /channels/"" — Firebase correctly denies that read, logging a
;; permission_denied error on every landing-page visit. Only subscribe to
;; the underlying Firebase path once a real channel exists.

;; Re-runs (and re-logs) on every Firebase update to this path, so a
;; single persistently-invalid entry logs repeatedly for as long as it
;; stays in the channel -- acceptable for now since the goal is
;; visibility, not a one-shot alert; revisit if this gets noisy enough
;; to need de-duplication.
(defn- validate-entities! [spec entities]
  (when (map? entities)
    (doseq [[id entity] entities]
      (specs/validate! spec id entity))))

(re-frame/reg-sub
 ::messages
 (fn [_]
   (let [channel @(re-frame/subscribe [::channel])]
     (when-not (string/blank? channel)
       (let [messages @(re-frame/subscribe [::db/realtime-value {:path (messages-path channel)}])]
         (validate-entities! ::specs/message messages)
         messages)))))

(re-frame/reg-sub
 ::clocks
 (fn [_]
   (let [channel @(re-frame/subscribe [::channel])]
     (when-not (string/blank? channel)
       (let [clocks @(re-frame/subscribe [::db/realtime-value {:path (clocks-path channel)}])]
         (validate-entities! ::specs/clock clocks)
         clocks)))))
