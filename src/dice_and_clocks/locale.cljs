(ns dice-and-clocks.locale
  "Locale is a personal, per-browser setting -- unlike the color theme
  (subs.cljs's ::theme, synced per-channel via Firebase), each viewer
  picks their own language independently, so this is local-only state
  backed by localStorage, not a Firebase realtime-value subscription.
  No auth/connectivity/channel race to guard against here (contrast
  views.cljs's comment on why ::theme can't subscribe on first render)."
  (:require [clojure.string :as string]
            [re-frame.core :as rf]
            [reagent.core :as r]
            [dice-and-clocks.i18n :as i18n]))

(def storage-key "dc-locale")

(defn- detect-browser-locale []
  (if (string/starts-with? (or (.. js/navigator -language) "") "ru")
    "ru"
    i18n/default-locale))

(defn- read-stored-locale []
  (let [v (.getItem js/localStorage storage-key)]
    (when (contains? i18n/supported-locales v) v)))

;; Auto-detected from navigator.language only on a first visit with no
;; stored preference; once set (by detection or by the toggle),
;; localStorage wins on every later visit -- navigator.language is
;; never consulted again.
(defonce locale-atom
  (r/atom (or (read-stored-locale) (detect-browser-locale))))

(defn set-locale! [locale]
  (when (contains? i18n/supported-locales locale)
    (.setItem js/localStorage storage-key locale)
    (reset! locale-atom locale)))

(rf/reg-sub ::locale (fn [_] @locale-atom))
