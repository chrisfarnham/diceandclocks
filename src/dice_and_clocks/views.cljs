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

(defmulti display-message (fn [message] (:message-type message)))

(defmethod display-message :default [message]
  (println (str "display-message: " message))
)

(defmethod display-message "message" [message]
  (let [{:keys [sender text] } message]
  [:div {:class "message"}
   [:div {:class "test"} (str sender " - " text)]]))


(defn add-message [persist-message]
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
  ])
)

(defn mark-deleted 
  "`message-path` includes the message-id"
  [message-path]
  (rf/dispatch
    [::db/push {:value true
                :path  (conj message-path :deleted?)}]))

(defn messages-list [context]
  (let [messages (reverse (:messages context))
        messages-path (:messages-path context)
        name (:name context)]
    
  [:<>
  [:div {:class "rounded-xl overflow-hidden bg-gradient-to-r from-gray-50 to-gray-100"}
  [:div {:class "p-2"}
  [add-message (fn [message]
                  (rf/dispatch
                  [::db/push {:value {:message-type :message :sender name :text message}
                              :path messages-path}]))]]

  [:div {:class "grid grid-cols-1 gap-1 p-1"}
   [:div {:class "mx-2"} [:p {:class "float-left prose prose-l"} "Events"]]
   (->> messages
        (remove (fn [[_ {:keys [deleted?]}]]
                  deleted?))
        (map (fn [[id {:keys [text sender] :as message}]]
               ^{:key id} ; https://stackoverflow.com/questions/33446913/reagent-react-clojurescript-warning-every-element-in-a-seq-should-have-a-unique
               [:div {:class "bg-gray-500 h-12 rounded-md flex p-2 relative"}
                [display-message message]
                ;; [:div {:class "message"}
                ;; [:div {:class ""} (str sender " - " text)]
                ;; ]
                [:div {:class "absolute right-2"}
                 [:button {:class "" :on-click #(mark-deleted (conj messages-path id))} "x"]
                 ]]
            )
        )
    )
   ]]]))

(defn channels-path [channel]
  [:channels channel])

(defn messages-path [channel]
  (conj (channels-path channel) :messages)
)

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
                                         :path channels-path}])
                            (rf/dispatch [:channel-name channel-name]))]]
            ; this is the main panel
            [:div {:class "grid grid-cols-2 divide-x divide-black"}
             [:div {:class "mr-2"}
              [messages-list context]]
             
             [:div {:class ""} [:p {:class "ml-2"}"This is more content"]]
            ]
          )
        ]
         [:div "Loading.."]))]]))
