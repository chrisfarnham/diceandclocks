(ns dice-and-clocks.firebase-database
  (:require [re-frame.core :as rf]
            [reagent.core :as r]
            [reagent.ratom :as ratom]
            [cljs-bean.core :refer [->js ->clj]]
            [clojure.string :as string]
            [dice-and-clocks.firebase-app :as firebase-app]
            ["firebase/database" :as fdb :refer [ref push set onValue]]))

(rf/reg-event-fx
 ::firebase-error
 (fn [_ [_ error]]
   (js/console.error (str "error:\n" error))))

(rf/reg-event-fx
 ::firebase-success
 (fn [_ [_]]
   (js/console.log (str "Write Succeeded"))))

(def default-pass-fail
  {:on-success [::firebase-success]
   :on-failure [::firebase-error]})

(defn success-failure-dispatch [args]
  (let [{:keys [on-success on-failure]} (merge default-pass-fail args)]
    (fn [err]
      (rf/dispatch
       (if (nil? err)
         on-success
         (conj on-failure err))))))

(defn ->path [p]
  (string/join "/" (->js p)))

(defn database-ref [path]
  (ref firebase-app/db (->path path)))

(defn- ref-set [{:keys [path value] :as args}]
  (let [respond (success-failure-dispatch args)]
    (-> (set (database-ref path) (->js value))
        (.then #(respond nil) respond))))

(rf/reg-fx ::set-fx
           (fn [args]
             (ref-set args)))

(rf/reg-event-fx
 ::set
 (fn [_ [_ args]]
   {::set-fx args}))

(defn get-push-key [path]
  (let [push-key (-> (database-ref path)
                      (push)
                      (.-key))]
    (concat path [push-key])))

(rf/reg-fx ::push-fx
           (fn [args]
             (ref-set
              (-> args
                  (update :path get-push-key)))))

(rf/reg-event-fx
 ::push
 (fn [_ [_ args]]
   {::push-fx args}))

(defn- ref-update [{:keys [path value] :as args}]
  (let [respond (success-failure-dispatch args)]
    (-> (fdb/update (database-ref path) (->js value))
        (.then #(respond nil) respond))))

(rf/reg-fx ::update-fx
           (fn [args]
             (ref-update args)))

(rf/reg-event-fx
 ::update
 (fn [_ [_ args]]
   {::update-fx args}))

(defn on-value-reaction
  "returns a reagent atom that will always have the latest value at 'path' in the Firebase database"
  [{:keys [path] :as args}]
  (let [query (database-ref path)
        reaction (r/atom nil)
        callback (fn [^js snapshot] (reset! reaction (some-> snapshot (.val) ->clj)))
        error-callback (success-failure-dispatch args)
        unsubscribe (onValue query callback error-callback)]
    (ratom/make-reaction
     (fn [] @reaction)
     :on-dispose unsubscribe)))

(rf/reg-sub ::realtime-value
            (fn [[_ args]]
              (on-value-reaction args))
            identity)
