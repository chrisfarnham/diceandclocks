(ns dice-and-clocks.firebase-app
  "Single place the Firebase app and its per-product service instances
  (db/auth/analytics) are created, per the modular SDK's
  initializeApp/getDatabase(app)/getAuth(app)/getAnalytics(app) pattern
  replacing the v8 compat SDK's global js/firebase object. Every other
  dice-and-clocks.firebase-* namespace requires this one and uses `db`/
  `auth`/`analytics` directly instead of reaching for a global -- the
  function-boundary seam PLAN-tooling-upgrade.md section 5 calls for,
  achieved by making the instances plain requirable defs rather than
  threading them through every event/subscription handler."
  (:require ["firebase/app" :refer [initializeApp]]
            ["firebase/database" :refer [getDatabase]]
            ["firebase/auth" :refer [getAuth]]
            ["firebase/analytics" :refer [getAnalytics]]
            ["./firebase_config.js" :refer [firebaseConfig]]
            [dice-and-clocks.config :as config]))

(defonce app (initializeApp firebaseConfig))
(defonce db (getDatabase app))
(defonce auth (getAuth app))
;; Only initialize the Analytics SDK at all when we'd actually use it --
;; getAnalytics(app) itself pulls in gtag.js, which can start collecting
;; automatic page-view/session data as soon as it's initialized,
;; independent of whether our own code ever calls logEvent. In dev and
;; on preview-channel deploys (config/track-analytics? false) `analytics`
;; stays nil; firebase_analytics.cljs's log-event already no-ops when
;; track-analytics? is false, so it also has to tolerate a nil instance.
(defonce analytics (when (config/track-analytics?) (getAnalytics app)))
