(ns dice-and-clocks.views
  (:require
   [clojure.string :as string]
   [dice-and-clocks.action-rolls :as action-rolls]
   [dice-and-clocks.clocks :as clocks]
   [dice-and-clocks.intro-view :as intro-view]
   [dice-and-clocks.firebase-auth :as auth]
   [dice-and-clocks.firebase-database :as db]
   [dice-and-clocks.subs :as subs]
   [dice-and-clocks.utils :as utils]
   [goog.string :as gstring]
   [re-frame.core :as rf]
   [reagent.core :as r]
   [haikunator :as Haikunator]
   ))


(def text-input-class "px-3 py-3 placeholder-gray-400 text-gray-700 relative bg-white bg-white rounded text-sm shadow outline-none focus:outline-none focus:shadow-outline w-3/4")

(def button-class "bg-grey-500 p-1 m-1 border-2 border-black print:hidden")


(defn auth-display [user]
  [:div {:class "inline-block"}
   [:div {:class ""}
    (if (:email user)
      [:span {:class ""} (or (:displayName user) (:email user))]
      [:span "Anonymous"])
    [:button {:class button-class
              :on-click #(rf/dispatch [(if user ::auth/sign-out ::auth/sign-in)])}
     (if (:email user)
       "Sign out"
       "Sign in")]]])


(def haikunator (new Haikunator (clj->js {:defaults {:tokenLength 8 :delimiter "-"}})))

(defn create-channel-id []
  (.haikunate haikunator))

(defn channel-name-ready? [channel-name]
  (not (some string/blank? (vals channel-name))))

(defn add-channel [context persist-channel-name]
  (let [{:keys [name channel]} context
        channel (if (= "" channel) (create-channel-id) channel)]
  (r/with-let [new-channel-name (r/atom {:channel channel :name name})]
   
  [:div {:class "space-y-2 space-x-2 text-center"}
   [:span {:class "block text-2xl"} "Start"]
   [:span {:class "block" }
       [:p {:class "text-xs"} "Your channel name is a shared secret for your group."]
   [:input {:type :text
            :class text-input-class
            :value (:channel @new-channel-name)
            :placeholder (if (= "" (:channel @new-channel-name)) "Channel Name" (:channel @new-channel-name))
            :on-change (fn [^js e] (swap! new-channel-name assoc :channel (.. e -target -value)))}]
    ]
   [:span {:class "block"}
    [:input {:type :text
            :class text-input-class
            :value (:name @new-channel-name)
            :placeholder "User Name"
            :on-change (fn [^js e] (swap! new-channel-name assoc :name (.. e -target -value)))}]]
   [:span {:class "block"}
   [:button {:disabled (not (channel-name-ready? @new-channel-name))
             :class button-class
             :on-click (fn []
                         (persist-channel-name @new-channel-name)
                         (reset! new-channel-name {:channel "" :name ""}))} "Join"]]])))

(def int-dice-map 
  {1 "fas fa-dice-one"
   2 "fas fa-dice-two"
   3 "fas fa-dice-three"
   4 "fas fa-dice-four"
   5 "fas fa-dice-five"
   6 "fas fa-dice-six"})

(defn int-to-dice 
  ([die-result]
   (int-to-dice die-result nil))
  ([die-result id]
   (let [die-class (get int-dice-map die-result)]
     ^{:key id}[:i {:class (str die-class " text-4xl m-1")}])))

(defn mark-deleted
  "`message-path` includes the message-id"
  [message-path]
  (rf/dispatch
   [::db/push {:value true :path  (conj message-path :deleted?)}]))


(defn message-container [context message display & {:keys [deleteable?] :or {deleteable? true}}]
   (let  [{:keys [id]} message
          {:keys [messages-path]} context]
     ^{:key id} ; https://stackoverflow.com/questions/33446913/reagent-react-clojurescript-warning-every-element-in-a-seq-should-have-a-unique
     [:div {:class "bg-gray-300 min-h-12 rounded-md flex p-2 relative"}
      (display)
      (when deleteable?
        [:div {:class "absolute right-2"}
         [:button {:class "text-white" :on-click #(mark-deleted (conj messages-path id))} "x"]])
    ]))

(defmulti display-message (fn [_ message] (:message-type message)))

(defmethod display-message "message" [context message]
  (let [{:keys [sender text] } message]
  (message-container context message (fn []
  [:div {:class "message"}
   [:div {:class ""} (str sender " - " text)]]))
  ))


(defmethod display-message "clock-deleted" [context message]
(let [{:keys [sender clock-path caption]} message]
  (message-container context message (fn []
  [:div {:class ""}
   [:span (str "\"" caption "\"")]
   [:div {:class "space-x-4"}(str sender " deleted a clock.") 
    [:button {:class button-class
              :on-click (fn [] (rf/dispatch [::db/update {:path clock-path :value {:deleted? nil}}]))} "Restore"]]
  ])
:deleteable? false)))

(defmethod display-message "clock-event" [context message]
  (let [{:keys [sender text caption key tic]} message]
    (message-container
     context message
     (fn []
       [:div {:class ""}
        [:span {:class ""} (str "\"" caption "\"")]
        [:div {:class "space-x-4"}
         [:span {:class "inline-block"} (str sender " " text)]
         [:span {:class "inline-block"} [:img {:class "inline w-8" :src (str "images/clocks/" (clocks/get-face key tic))}]]]]) 
     :deleteable? false)))

(defmethod display-message "dice-roll" [context message]
  (let [{:keys [id sender result pool text size position effect critical]} message]
    (message-container context message (fn []
    [:div {:class "w-full grid grid-cols-2"}
     [:div {:class "inline-block align-middle"}
      [:div (str sender)]
      [:span {:class ""}
       [:span {:class "inline-block align-bottom"}
        (map-indexed (fn [index item] (int-to-dice item (str id "-" index))) pool)]
       [:span {:class "text-4xl align-middle"} (str " : " result)]
       [:span {:class "text-xs italic"} (str " (" size " dice)") [:br]]]
      [:div (when-not (string/blank? text) [:span (str "\"" text "\"") [:br]])]]
     [:div {:class ""}
      [:div {:class "text-center text-xl"}
      (if (string/blank? position)
        (when (= critical true) "Critical!")
        (str position " ~ " effect))]
      [:div {:class "text-sm ml-4"}
             (when-not (string/blank? position) 
               [action-rolls/result-description result position critical]
             )
       ]
      
     ]
    ])
)))

(defmethod display-message :default [_ message]
  (println (str "default display-message: " message)))

(defn create-message [name message]
  {:message-type "message" :sender name :text message})

(defn persist-roll [context dice-results]
  (rf/dispatch [::db/push {:path (:messages-path context) 
                           :value (merge dice-results
                                         {:sender (:name context)
                                          :message-type "dice-roll"})}])
)

(def circle-button-class "text-sm fas fa-circle")
(def little-div-class "h-3")


(defn position-and-effect [on-mouse-over on-mouse-out on-click]
  [:div {:class "relative"}
  [:div {:class "container absolute inset-y-0 right-0 w-14 h-10 grid grid-cols-4"}
   (map-indexed 
    (fn [idx item] 
      (let [{:keys [position effect]} item]
      ^{:key (str position "-" effect)}
        [:<>
         [:div {:class little-div-class} 
          [:button {:class "focus:outline-none"
                    :on-click #(on-click position effect)
                    :on-mouse-over #(on-mouse-over position effect)
                    :on-mouse-out  #(on-mouse-out)
                    }
           [:i {:class circle-button-class}]]]
         (cond (= 8 idx) [:button {:class (str "text-white p-px focus:outline-none " little-div-class) 
                                   :on-click #(on-click nil nil)} "x"]
               (= 2 (mod idx 3)) [:div {:class little-div-class} ""])]
        )) action-rolls/combinations)]]
)

(def proto-dice-roll {:size 0 :position nil :effect nil :text nil})

(defn roll-dice [context]
  (r/with-let [dice-roll (r/atom proto-dice-roll) p-and-e-label (r/atom nil)]
    (letfn [(increment [] (when (< (:size @dice-roll) 9) (swap! dice-roll assoc :size (inc (:size @dice-roll)))))
            (decrement [] (when (< 0 (:size @dice-roll)) (swap! dice-roll assoc :size (dec (:size @dice-roll)))))
            (roll[] (persist-roll context (merge @dice-roll
                                                 (action-rolls/generate-dice-results (:size @dice-roll)))) 
                 (reset! dice-roll proto-dice-roll))
            (position-and-effect-set? [] (let [{:keys [position effect]} @dice-roll](not-any? nil? [position effect])))
            (on-mouse-over [position effect] (reset! p-and-e-label (str position " ~ " effect)))
            (on-mouse-out [] (reset! p-and-e-label nil))
            (on-click [position effect] (swap! dice-roll assoc :position position :effect effect))
            ]
      [:<>
       [:div {:class "bg-gray-300 grid grid-cols-3 grid-rows-2 p-1"}
        [:div {:class "grid grid-cols-2"}
         (position-and-effect on-mouse-over on-mouse-out on-click)
         [:div {:class "w-64"}
          [:button {:class button-class
                    :on-click (fn [] (decrement))} "-"]
          (str (:size @dice-roll))
          [:button {:class button-class
                    :on-click (fn [] (increment))} "+"]]]
        [:div {:class "col-span-2 relative"}
         [:input {:type :text
                  :class (str text-input-class "")
                  :value (:text @dice-roll)
                  :placeholder "Roll caption"
                  :max-length "100"
                  :on-change (fn [^js e] (swap! dice-roll assoc :text (.. e -target -value)))}]
         [:button {:class (str "absolute inset-y-0 right-0" button-class)
                   :on-click (fn [] (roll))} "Roll"]]
        [:div {:class "col-span-2"}
         [:p {:class (str "mt-3 text-2xl text-center align-middle" 
                          (when-not (position-and-effect-set?) " text-gray-900 text-opacity-70 animate-pulse"))}
          (if (position-and-effect-set?)
            (let [{:keys [position effect]} @dice-roll] (str position " ~ " effect))
            @p-and-e-label)]
        ]
        [:div]
        ]]
       )))


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
              :placeholder "Message"
              :max-length "100"
              :on-change
              (fn [^js e] (reset! new-message (.. e -target -value)))}]
     [:button {:disabled (string/blank? @new-message)
               :class button-class
               :on-click (fn []
                           (persist-message @new-message)
                           (reset! new-message nil))} "Send"]])))
)

(def content-box-class "container rounded-xl bg-gradient-to-r from-gray-50 to-gray-100")

(defn process-message
  "Destructure the id and add it to the message map as a field"
[message]
(let [[id  message] message] (assoc message :id id))
)

(defn messages-list [context]
  (let [messages (reverse (:messages context))
        messages (->> messages (map process-message))]
  [:<>
  [:div {:class content-box-class}
  [:div {:class "p-2"} [roll-dice context]]
  [:div {:class "grid grid-flow-row grid-cols-1"}
   [:div {:class "mx-2 p-2 bg-gray-300"} 
    [:span {:class "float-left w-full"} [:div {:class ""}[add-message context]]]]
   [:div {:class "overscroll-auto overflow-auto max-h-118 grid m-1 gap-1 p-1"}

    (->> messages
         (remove (fn [{:keys [deleted?]}] deleted?))
         ;(map (fn [message] [:p "Message would go here!"]))
         (map (fn [message] (display-message context message)))
         )]]]]
))

(defn create-clock [context key caption]
  (let [{:keys [name messages-path clocks-path clock-count]} context
        clock {:key key :creator name :caption caption :tic 0 :order clock-count}
        clock-message {:message-type "clock-event" :sender name :text "created a new clock"}
        clock-message (merge clock-message clock)]

    (rf/dispatch [::db/push {:path clocks-path :value clock}])
    (rf/dispatch [::db/push {:path messages-path :value clock-message}])
))

(defn mark-clock-deleted
  "`message-path` includes the message-id"
  [context clock-path caption]
  (let[{:keys [name messages-path]} context]
  (rf/dispatch
   [::db/push {:value true :path  (conj clock-path :deleted?)}])
    (println (str "sender " name " clock-path " clock-path))
  (rf/dispatch
   [::db/push {:path messages-path 
               :value {:message-type "clock-deleted" :sender name :clock-path clock-path :caption caption}}])
  ))

(def clock-button-class "px-1 text-3xl font-extra-bold")

(defn display-clock [context clock]
  (let [{:keys [clocks-path messages-path name]} context
        {:keys [key tic id caption creator]} clock
        this-clock-path (conj clocks-path id)
        clock-face (clocks/get-face key tic)]
  (letfn [(advance [] (when (< tic (clocks/max-index key))
                        (let [clock  (update clock :tic inc)
                              new-values {:tic (:tic clock)}
                              clock-message {:message-type "clock-event" :sender name :text "advanced a clock"}
                              clock-message (merge clock-message clock)]
                        (rf/dispatch [::db/update {:path this-clock-path :value new-values}])
                        (rf/dispatch [::db/push {:path messages-path :value clock-message}])
                        )))
          (roll-back [] (when (< 0 tic)
                        (let [clock  (update clock :tic dec)
                              new-values {:tic (:tic clock)}
                              clock-message {:message-type "clock-event" :sender name :text "rolled back a clock"}
                              clock-message (merge clock-message clock)]
                          (rf/dispatch [::db/update {:path this-clock-path :value new-values}])
                          (rf/dispatch [::db/push {:path messages-path :value clock-message}]))
                          ))]
    ^{:key id}
    [:div {:class "bg-gray-200 relative"}
        [:div {:class "absolute top-2 right-4"}
         [:button {:class "print:hidden" :on-click #(mark-clock-deleted context this-clock-path caption)} "x"]]
    [:div {:class "h-full m-px p-2 bg-gray-300"}
     [:img  {:class "w-24" :src (str "images/clocks/" clock-face)}]
     [:span {:class "inline-block print:hidden"}
      [:button {:class clock-button-class :on-click #(advance)} "+"]
      [:button {:class clock-button-class :on-click #(roll-back)} "-"]]
     [:div {:class "text-lg prose prose-m"} caption]
     [:div {:class "text-xs"} creator]
     ]
     ]
)))

; overscroll-auto overflow-auto max-h-screen grid m-1 gap-1 p-1
(defn display-clocks [context]
  (let [{:keys [clocks]} context
         clocks (reverse clocks)
         clocks (->> clocks (map process-message))]
  [:div {:class content-box-class}
   [:div {:class "p-2"}
     [:div {:class "bg-gray-300 p-3"}
   [:div {:class "overscroll-auto overflow-auto max-h-118 grid grid grid-cols-3 flex relative print:container print:overflow-visible"}
        (->> clocks
         (remove (fn [{:keys [deleted?]}] deleted?))
         (map (fn [clock] (display-clock context clock))))
    (when (< 1 (count clocks))
    [:div {:class "col-span-3 text-xs text-center print:hidden"} 
     "Hint: Share clock state with your players when your session is done by printing this page to PDF and emailing it to them."])
    ]
  ]]]
))

(defn clocks-list [context]
  (r/with-let [caption (r/atom "")]
  (letfn [(click-clock [clock-key] (create-clock context clock-key @caption)(reset! caption ""))]
  [:div {:class content-box-class}
   [:div {:class "p-2 print:hidden"}
    [:div {:class "bg-gray-300 p-3"}
     [:input {:type  :text
              :class text-input-class
              :value @caption
              :placeholder "Clock caption"
              :max-length "100"
              :on-change
              (fn [^js e] (reset! caption (.. e -target -value)))}]
     [:div {:class "grid grid-cols-12 p-2"}
      (map (fn [{:keys [key face]} _]
             ^{:key key} [:button {:on-click #(click-clock key)}
                          [:img {:class "w-8" :src (str "images/clocks/" face)}]])
           clocks/clock-types)]]]
    [display-clocks context]]
  )))

(defn channels-path [channel]
  [:channels (keyword channel)])

(defn messages-path [channel]
  (conj (channels-path channel) :messages))

(defn clocks-path [channel]
  (conj (channels-path channel) :clocks))


(defn main-panel []
  (let [name @(rf/subscribe [::subs/name])
        user @(rf/subscribe [::auth/user-auth])
        db-connected? @(rf/subscribe [::db/realtime-value {:path [:.info :connected]}])
        channel @(rf/subscribe [::subs/channel])
        messages @(rf/subscribe [::db/realtime-value {:path (messages-path channel)}])
        clocks @(rf/subscribe [::db/realtime-value {:path (clocks-path channel)}])
        channel-name {:channel channel :name name}
        context {:name name 
                 :user user 
                 :channel channel 
                 :messages messages
                 :clocks clocks
                 :clock-count (count clocks)
                 :channels-path (channels-path channel)
                 :messages-path (messages-path channel)
                 :clocks-path (clocks-path channel)}]
    [:div {:class "h-screen"}
     [:div {:class "flex flex-col w-full h-screen fixed pin-l pin-y bg-gray-300"}
      [:div {:class "grid grid-cols-3 mt-1"}
       [:div {:class "ml-1"}[:p {:class "float-left prose prose-xl"} "Clocks and Dice"]]
       [:div {:class "text-sm text-center"}
        (when (channel-name-ready? channel-name)
          [:span
          [:p {:class "print:hidden"} "Copy and share this address "]
          [:p {:class "font-mono"} (str utils/shareable-address)]])]
       [:div {:class "float-right text-right"} [auth-display user]]]
      (if-not user 
        [:div {:class "container mx-auto flex flex-wrap content-center"}
        [:div {:class " "} (intro-view/intro-view [auth-display user]) ]
        ]
        (if db-connected?
          [:div {:class "p-2"}
           (if (not (channel-name-ready? channel-name))
             [:div {:class "absolute"}
              (intro-view/intro-view
              [add-channel context
               (fn [channel-name]
                 (let [channel-name (assoc channel-name :channel (utils/slugify (:channel channel-name)))]
                   (rf/dispatch [:channel-name channel-name])))])
              ]
             [:div {:class "grid grid-cols-2 print:grid-cols-none"}
                   (rf/dispatch
                    [::db/update {:value {:last-accessed (.now js/Date)}
                                :path (:channels-path context)}])
              [:div {:class "mr-2 print:hidden"}
               [messages-list context]]
              [:div {:class "ml-2"}
               [clocks-list context]]]
          )]
          ; if db not connected
          [:div "Loading..."]
        )
      )
      ]]))
