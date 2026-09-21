(ns dice-and-clocks.firebase-auth
  (:require [re-frame.core :as rf]
            [reagent.core :as r]))

(defn auth ^js [] (.auth ^js js/firebase))

(defn sign-in [auth-provider opts]
  (-> (auth)
      (.signInWithPopup auth-provider)
      (.catch (fn [e]
                (if-let [handler (:error-handler opts)]
                  (handler e)
                  (js/alert e))))))

(defn google-sign-in [opts]
  (sign-in (js/firebase.auth.GoogleAuthProvider.) opts))

(rf/reg-fx ::google-sign-in  google-sign-in)

(defn sign-out [error-handler]
  (-> (auth)
      (.signOut)
      (.catch (fn [e] (if error-handler (error-handler e) (js/console.log e)))))
  (set! (.-location js/window) "/"))

(rf/reg-fx ::sign-out sign-out)

(defn user->data [^js user]
  (when user
    {:email        (.-email user)
     :uid          (.-uid user)
     :display-name (.-displayName user)}))

(defn user-info []
  (let [auth-state (r/atom nil)
        callback (fn [x]
                   (reset! auth-state (user->data x)))
        error-callback (fn [x] (reset! auth-state x))]
    (.onAuthStateChanged (auth)
                         callback
                         error-callback)
    auth-state))

(rf/reg-sub ::user-auth
            user-info
            (fn [user]
              (let [errored? (instance? js/Error user)]
                (when (or (not user) errored?)
                  ;; Trigger anonymous sign-in as a side effect, but
                  ;; always return the real auth state below (nil while
                  ;; pending) rather than signInAnonymously's Promise --
                  ;; a Promise is truthy, so returning it here would make
                  ;; every downstream (if-not user ...) check see a
                  ;; signed-in user immediately, before
                  ;; onAuthStateChanged's callback (which is what
                  ;; actually updates user-info's atom) has fired even
                  ;; once.
                  (-> (auth) (.signInAnonymously)))
                (when-not errored? user))))

(rf/reg-sub ::uid
            (fn [] (rf/subscribe [::user-auth]))
            (fn [auth]
              (when auth
                (:uid auth))))

(rf/reg-event-fx
 ::sign-in
 (fn [_ _] {::google-sign-in nil}))

(rf/reg-event-fx
 ::sign-out
 (fn [_ _] {::sign-out nil}))