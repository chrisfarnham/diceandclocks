(ns dice-and-clocks.views
  (:require
   [re-frame.core :as rf]
   [reagent.core :as r]
   [goog.string :as gstring]
   [clojure.string :as string]
   [dice-and-clocks.firebase-auth :as auth]
   [dice-and-clocks.firebase-database :as db]
   [dice-and-clocks.subs :as subs]
   [dice-and-clocks.config :as config]
   ))

(defn auth-display [user]
  [:div
   (when user
     [:span (str "Logged in as: " (or (:displayName user) (:email user)))])
   [:br]
   [:button {:on-click #(rf/dispatch [(if user ::auth/sign-out ::auth/sign-in)])}
    (if user
      "Sign out"
      "Sign in")]])

(def text-input-class "px-3 py-3 placeholder-gray-400 text-gray-700 relative bg-white bg-white rounded text-sm shadow outline-none focus:outline-none focus:shadow-outline")

(def button-class "bg-grey-500 text-white active:bg-pink-600 font-bold uppercase text-sm px-6 py-3 rounded shadow hover:shadow-lg outline-none focus:outline-none mr-1 mb-1")

(declare new-channel)
(defn add-channel [persist-channel]
  (r/with-let [new-channel (r/atom nil)]
  [:<>
   [:div "Create a channel"]
   [:input {:type :text
            :class text-input-class
            :value @new-channel
            :on-change (fn [^js e] (reset! new-channel (.. e -target -value)))}]
   [:button {:disabled (string/blank? @new-channel)
             :class button-class
             :on-click (fn []
                         (persist-channel @new-channel)
                         (reset! new-channel nil))} "Create"]]))
(declare new-todo)
(defn add-todo [persist-todo]
  (r/with-let [new-todo (r/atom nil)]
    [:<>
     [:div "Input new Todo:"]
     [:input {:type  :text
              :class text-input-class
              :value @new-todo
              :on-change
              (fn [^js e] (reset! new-todo (.. e -target -value)))}]
     [:button {:disabled (string/blank? @new-todo)
               :class button-class
               :on-click (fn []
                           (persist-todo @new-todo)
                           (reset! new-todo nil))} "Save"]]))

(defn todo-list [todos mark-done]
  [:div
   [:h2 "Todos"]
   (->> todos
        (remove (fn [[_ {:keys [done?]}]]
                  done?))
        (map (fn [[id {:keys [description]}]]
               ^{:key id}
               [:div {:style {:padding "5px"
                              :border-bottom "solid"
                              :border-color "gray"}}
                [:span {:style {:margin-right "5px"}} description]
                [:button {:on-click #(mark-done id)} "Done"]])))])


(defn main-panel []
  (let [name @(rf/subscribe [::subs/name])
        user @(rf/subscribe [::auth/user-auth])
        db-connected? @(rf/subscribe [::db/realtime-value {:path [:.info :connected]}])
        channels-path [:channels]
        channel @(rf/subscribe [::subs/channel])
        todos-path [:users (:uid user) :todos]
        todos @(rf/subscribe [::db/realtime-value {:path todos-path}])]
    [:div {:class "w-full bg-gray-300"}
     [:h1 "Clocks and Dice"]
     [:h1 (str "Channel: " channel)]
     [:p {:class "text-gray-500"} "This is a test"]
     [auth-display user]
(when user
  (if db-connected?
    [:div {:class "p-6"}
     [add-channel (fn [channel]
                    (rf/dispatch
                     [::db/push {:value {:name channel}
                                 :path channels-path}])
                    (rf/dispatch [:channel channel]))]
     [:br]
     [add-todo (fn [todo]
                 (rf/dispatch
                  [::db/push {:value {:description todo}
                              :path  todos-path}]))]
     [todo-list todos
      (fn [todo-id]
        (rf/dispatch
         [::db/push {:value true
                     :path  (concat todos-path [todo-id :done?])}]))]]
    [:div "Loading.."]))
     ]))
