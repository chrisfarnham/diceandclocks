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



(def text-input-class "px-3 py-3 placeholder-gray-400 text-gray-700 relative bg-white bg-white rounded text-sm shadow outline-none focus:outline-none focus:shadow-outline w-1/2")

(def button-class "bg-grey-500 p-1 m-1 border-2 border-black")


(defn auth-display [user]
  [:div {:class "inline-block align-middle"}
   (when user
     [:span {:class "" }(or (:displayName user) (:email user))])
   [:div {:class "float-right"}
   [:button {:class button-class
             :on-click #(rf/dispatch [(if user ::auth/sign-out ::auth/sign-in)])}
    (if user
      "Sign out"
      "Sign in")]]])

(defn channel-name-ready? [channel-name]
  (some string/blank? (vals channel-name)))

(defn add-channel [persist-channel-name]
  (r/with-let [new-channel-name (r/atom {:channel "" :name ""})]
  [:div {:class "space-y-4 w-1/2 self-center items-center"}
   [:span {:class "block"} "Start"]
   [:span {:class "block" }
    
   [:input {:type :text
            :class text-input-class
            :value (:channel @new-channel-name)
            :placeholder "Channel Name"
            :on-change (fn [^js e] (swap! new-channel-name assoc :channel (.. e -target -value)))}]]
   [:span {:class "block"}
    [:input {:type :text
            :class text-input-class
            :value (:name @new-channel-name)
            :placeholder "User Name"
            :on-change (fn [^js e] (swap! new-channel-name assoc :name (.. e -target -value)))}]]
   [:span {:class "block"}
   [:button {:disabled (channel-name-ready? @new-channel-name)
             :class button-class
             :on-click (fn []
                         (persist-channel-name @new-channel-name)
                         (reset! new-channel-name {:channel "" :name ""}))} "Create"]]]))

(def int-dice-map 
  {1 "fas fa-dice-one"
   2 "fas fa-dice-two"
   3 "fas fa-dice-three"
   4 "fas fa-dice-four"
   5 "fas fa-dice-five"
   6 "fas fa-dice-six"})

(defn int-to-dice [die-result]
  (let [die-class (get int-dice-map die-result)]
  [:<>[:i {:class (str die-class " text-4xl m-1")}]]))

(defmulti display-message (fn [message] (:message-type message)))

(defmethod display-message "message" [message]
  (let [{:keys [sender text] } message]
  [:div {:class "message"}
   [:div {:class ""} (str sender " - " text)]]))

(defmethod display-message "dice-roll" [message]
  (let [{:keys [sender result pool text size]} message]
    [:div {:class "overflow-visible"}
      (str "\"" text "\"") [:br]
     [:span {:class "inline-block align-middle"}
      (str sender " rolled ")
      [:span {:class "inline-block align-bottom"}
      (map-indexed (fn [index item] ^{:key index} (int-to-dice item)) pool)]
      " for a result of " [:span {:class "text-4xl"} (str result)]
      (str " (" size " dice)"  )
      ]]))

(defmethod display-message :default [message]
  (println (str "display-message: " message)))

(defn create-message [name message]
  {:message-type "message" :sender name :text message})


(defn mark-deleted
  "`message-path` includes the message-id"
  [message-path]
  (rf/dispatch
   [::db/push {:value true :path  (conj message-path :deleted?)}]))

(defn message-container [context message]
  (let  [[id {:keys [text sender] :as m}] message
         {:keys [messages-path]} context]
    ^{:key id} ; https://stackoverflow.com/questions/33446913/reagent-react-clojurescript-warning-every-element-in-a-seq-should-have-a-unique
    [:div {:class "bg-gray-500 min-h-12 rounded-md flex p-2 relative"}
     [display-message m]
     [:div {:class "absolute right-2"}
      [:button {:class "" :on-click #(mark-deleted (conj messages-path id))} "x"]]]))

(defn generate-dice-results [size]
  (let [pool-size (if (< size 1) 2 size)
        pool (repeatedly pool-size #(+ 1 (rand-int 6)))
        result (if (< size 1) (apply min pool) (apply max pool))
        ; zero size dice pools cannot result in crits
        critical (and (< 0 size) (< 1 (count (filter #(= 6 %) pool))))]
    {:pool (vec pool) :result result :size size :critical critical}))

(defn persist-roll [context dice-results]
  (rf/dispatch [::db/push {:path (:messages-path context) 
                           :value (merge dice-results
                                         {:sender (:name context)
                                          :message-type "dice-roll"})}])
)

(def proto-dice-roll {:size 0 :position "" :effect "" :text ""})

(defn roll-dice [context]
  (r/with-let [dice-roll (r/atom proto-dice-roll)]
    (letfn [(increment [] (when (< (:size @dice-roll) 9) (swap! dice-roll assoc :size (inc (:size @dice-roll)))))
            (decrement [] (when (< 0 (:size @dice-roll)) (swap! dice-roll assoc :size (dec (:size @dice-roll)))))
            (roll[] (persist-roll context (merge @dice-roll 
                                                 (generate-dice-results (:size @dice-roll)))) 
                 (reset! dice-roll proto-dice-roll))]
      [:<>
       [:div {:class "bg-gray-300 p-3"}

        [:button {:class button-class
                  :on-click (fn [] (decrement))} "-"]
        (str (:size @dice-roll))
        [:button {:class button-class
                  :on-click (fn [] (increment))} "+"]
        [:br]
        ;  :on-change (fn [^js e] (swap! new-channel-name assoc :name (.. e -target -value)))
        [:input {:type :text
            :class text-input-class
            :value (:text @dice-roll)
            :placeholder ""
            :on-change (fn [^js e] (swap! dice-roll assoc :text (.. e -target -value)))
                 }]
        [:button {:class button-class
                  :on-click (fn [] (roll))} "Roll"]
      ]])))


(defn add-message [context]
  (let [{:keys [messages-path name]} context]
  (letfn [(persist-message [message]
            (rf/dispatch
             [::db/push {:value (create-message name message)
                         :path messages-path}]))]
  (r/with-let [new-message (r/atom nil)]
  [:<>
         [:input {:type  :text
              :class text-input-class
              :value @new-message
              :on-change
              (fn [^js e] (reset! new-message (.. e -target -value)))}]
     [:button {:disabled (string/blank? @new-message)
               :class button-class
               :on-click (fn []
                           (persist-message @new-message)
                           (reset! new-message nil))} "Send"]
  ])))
)


(defn messages-list [context]
  (let [messages (reverse (:messages context))]
  [:<>
  [:div {:class "container rounded-xl bg-gradient-to-r from-gray-50 to-gray-100"}
  ;[:div {:class "p-2"} [add-message context]]
  [:div {:class "p-2"} [roll-dice context]]
  [:div {:class "grid grid-flow-row grid-cols-1"}
   [:div {:class "mx-2"} 
    [:span {:class "float-left text-2xl prose prose-l"} "Events" ]
    [:span {:class "float-right w-3/4"} [:div {:class "text-right"}[add-message context]]]]
   [:div {:class "overscroll-auto overflow-auto max-h-screen grid m-1 gap-1 p-1"}
   (->> messages
        (remove (fn [[_ {:keys [deleted?]}]] deleted?))
        (map #(message-container context %)))
   ]]]])
)

(defn channels-path [channel]
  [:channels (str channel)])

(defn messages-path [channel]
  (println (str "channel: " channel))
  (conj (channels-path channel) :messages))

(defn main-panel []
  (let [name @(rf/subscribe [::subs/name])
        user @(rf/subscribe [::auth/user-auth])
        db-connected? @(rf/subscribe [::db/realtime-value {:path [:.info :connected]}])
        channel @(rf/subscribe [::subs/channel])
        messages @(rf/subscribe [::db/realtime-value {:path (messages-path channel)}])
        context {:name name 
                 :user user 
                 :channel channel 
                 :messages messages
                 :channels-path (channels-path channel)
                 :messages-path (messages-path channel)}]
    [:div {:class "h-screen"}
     [:div {:class "flex flex-col w-full h-screen fixed pin-l pin-y bg-gray-300"}
      [:div {:class "block"}
       [:p {:class "float-left prose prose-xl"} "Clocks and Dice"]
       [:div {:class "float-right"} [auth-display user]]]
      (when user
        (if db-connected?
          [:div {:class "p-2"}
           (if (channel-name-ready? {:channel channel :name name})
             [:div
              [add-channel (fn [channel-name]
                             (rf/dispatch
                              [::db/push {:value (:channel channel-name)
                                          :path (channels-path (:channel channel-name))}])
                             (rf/dispatch [:channel-name channel-name]))]]
            ; this is the main panel
             [:div {:class "grid grid-cols-2 divide-x divide-black"}
              [:div {:class "mr-2"}
               [messages-list context]]
              [:div {:class ""} [:p {:class "ml-2"} "This is more content"]]])]
          ; if db not connected
          [:div "Loading.."]
        ))]]))
