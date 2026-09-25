(ns dice-and-clocks.views
  (:require
   [clojure.string :as string]
   [dice-and-clocks.action-rolls :as action-rolls]
   [dice-and-clocks.clocks :as clocks]
   [dice-and-clocks.config :as config]
   [dice-and-clocks.i18n :as i18n]
   [dice-and-clocks.intro-view :as intro-view]
   [dice-and-clocks.locale :as locale]
   [dice-and-clocks.firebase-auth :as auth]
   [dice-and-clocks.firebase-database :as db]
   [dice-and-clocks.firebase-analytics :as analytics]
   [dice-and-clocks.subs :as subs]
   [dice-and-clocks.utils :as utils]
   [re-frame.core :as rf]
   [reagent.core :as r]
   [haikunator :as Haikunator]
   [html-to-image]
   [downloadjs]
   ))


(def text-input-class "px-3 py-3 placeholder-gray-400 dc-input relative rounded text-sm shadow outline-none focus:outline-none focus:shadow-outline w-3/4")

(def button-class "dc-btn p-1 m-1 border-2 print:hidden")


(defn auth-display []
  [:div {:class "inline-block p-px"}
      [:i {:class "fas fa-cookie-bite"}]]) ; left blank on purpose


(def haikunator (new Haikunator (clj->js {:defaults {:tokenLength 8 :delimiter "-"}})))

(defn create-channel-id []
  (.haikunate haikunator))

(defn channel-name-ready? [channel-name]
  (not (some string/blank? (vals channel-name))))

(defn add-channel [persist-channel-name]
  (let [name @(rf/subscribe [::subs/name])
        channel @(rf/subscribe [::subs/channel])
        channel (if (string/blank? channel) (create-channel-id) channel)
        locale @(rf/subscribe [::locale/locale])]
    (r/with-let [new-channel-name (r/atom {:channel channel :name name})]
      [:div {:class "space-y-2 text-center"}
       [:p {:class "text-2xl dc-title-font"} (i18n/t :ui/start locale)]
       [:div
        [:p {:class "text-xs"} (i18n/t :ui/channel-secret-hint locale)]
        [:input {:type :text
                 :class text-input-class
                 :value (:channel @new-channel-name)
                 :placeholder (i18n/t :ui/channel-name-placeholder locale)
                 :on-change (fn [^js e] (swap! new-channel-name assoc :channel (.. e -target -value)))}]]
       [:div
        [:input {:type :text
                 :class text-input-class
                 :value (:name @new-channel-name)
                 :placeholder (i18n/t :ui/user-name-placeholder locale)
                 :on-change (fn [^js e] (swap! new-channel-name assoc :name (.. e -target -value)))}]]
       [:div
        [:button {:disabled (not (channel-name-ready? @new-channel-name))
                  :class button-class
                  :on-click (fn []
                              (persist-channel-name @new-channel-name)
                              (reset! new-channel-name {:channel "" :name ""}))} (i18n/t :ui/join locale)]]])))

(def dice-icon-class 
  {1 "fas fa-dice-one"
   2 "fas fa-dice-two"
   3 "fas fa-dice-three"
   4 "fas fa-dice-four"
   5 "fas fa-dice-five"
   6 "fas fa-dice-six"})

(defn dice-icon 
  ([die-result]
   (dice-icon die-result nil))
  ([die-result id]
   (let [die-class (get dice-icon-class die-result)]
     ^{:key id}[:i {:class (str die-class " text-4xl m-1")}])))

(defn message-container [message display & {:keys [deleteable?] :or {deleteable? true}}]
   (let  [{:keys [id]} message
          channel @(rf/subscribe [::subs/channel])
          messages-path (subs/messages-path channel)]
     [:div {:class "dc-card-bg dc-card-border border-2 rounded-md flex p-2 relative"}
      (display)
      (when deleteable?
        [:div {:class "absolute right-2"}
         [:button {:class "dc-delete-btn"
                   :on-click #(rf/dispatch [:mark-message-deleted (conj messages-path id)])} "x"]])
    ]))

(defmulti display-message (fn [message] (:message-type message)))

(defmethod display-message "message" [message]
  (let [{:keys [sender text] } message]
  (message-container message (fn []
  [:div {:class "message"}
   [:div {:class ""} (str sender " - " text)]]))
  ))


(defmethod display-message "clock-deleted" [message]
(let [{:keys [sender clock-path caption]} message
      locale @(rf/subscribe [::locale/locale])]
  (message-container message (fn []
  [:div {:class ""}
   [:span (i18n/tf :format/quote locale caption)]
   [:div {:class "space-x-4"} (i18n/tf :clock-event/deleted locale sender)
    [:button {:class button-class
              :on-click (fn [] (rf/dispatch [:restore-clock clock-path]))} (i18n/t :ui/restore locale)]]
  ])
:deleteable? false)))

(defmethod display-message "clock-event" [message]
  (let [{:keys [sender text caption key tic]} message
        locale @(rf/subscribe [::locale/locale])]
    (message-container
     message
     (fn []
       [:div {:class ""}
        [:span {:class ""} (i18n/tf :format/quote locale caption)]
        [:div {:class "space-x-4"}
         [:span {:class "inline-block"} (str sender " " text)]
         [:span {:class "inline-block"} [:img {:class "inline w-8 dc-clock-icon" :src (str "images/clocks/" (clocks/get-face key tic))}]]]])
     :deleteable? false)))

(defmethod display-message "dice-roll" [message]
  (let [{:keys [id sender result pool text size position effect critical]} message
        locale @(rf/subscribe [::locale/locale])]
    (message-container message (fn []
    [:<>
     (when critical
       [:div {:class "absolute inset-0 rounded-md animate-critical-flash pointer-events-none"}])
     [:div {:class "w-full grid grid-cols-2"}
     [:div {:class "inline-block align-middle"}
      [:div (str sender)]
      [:span {:class ""}
       [:span {:class "inline-block align-bottom"}
        (map-indexed (fn [index item] (dice-icon item (str id "-" index))) pool)]
       [:span {:class "text-4xl align-middle"} (str " : " result)]
       [:span {:class "text-xs italic"} (i18n/tf :dice/count-suffix locale size) [:br]]]
      [:div (when-not (string/blank? text) [:span (i18n/tf :format/quote locale text) [:br]])]]
     [:div {:class ""}
      [:div {:class "text-center text-xl"}
      (cond
        critical [:span {:class "text-3xl font-extrabold text-red-600 animate-critical-fade-in"} (i18n/t :ui/critical-banner locale)]
        (string/blank? position) nil
        :else (str (i18n/translate-position position locale) " ~ " (i18n/translate-effect effect locale)))]
      [:div {:class "text-sm ml-4"}
             (when (and (not critical) (not (string/blank? position)))
               [action-rolls/result-description result position critical locale]
             )
       ]

     ]]
    ])
)))

(defmethod display-message :default [message]
  (println (str "default display-message: " message)))

(def circle-button-class "text-lg fas fa-circle")
(def little-div-class "h-3")


(defn position-and-effect [{:keys [on-mouse-over on-mouse-out on-click]}]
  [:div {:class "relative"}
  [:div {:class "container absolute inset-y-0 right-0 w-16 h-12 grid grid-cols-4 gap-2"}
   (map-indexed 
    (fn [idx item] 
      (let [{:keys [position effect]} item]
      ^{:key (str position "-" effect)}
        [:<>
         [:div {:class little-div-class}
          [:button {:class "focus:outline-none"
                    :on-click #(on-click position effect)
                    :on-mouse-over #(on-mouse-over position effect)
                    :on-mouse-out  #(on-mouse-out)}
           [:i {:class circle-button-class}]]]
         (cond (= 8 idx) [:button {:class (str "dc-delete-btn p-px focus:outline-none " little-div-class) 
                                   :on-click #(on-click nil nil)} "x"]
               (= 2 (mod idx 3)) [:div {:class little-div-class} ""])]
        )) action-rolls/combinations)]]
)

(def proto-dice-roll {:size 0 :position nil :effect nil :text nil})

(defn roll-dice []
  (r/with-let [dice-roll (r/atom proto-dice-roll) p-and-e-label (r/atom nil)]
    (let [locale @(rf/subscribe [::locale/locale])
          increment (fn [] (swap! dice-roll update :size #(min 9 (inc %))))
          decrement (fn [] (swap! dice-roll update :size #(max 0 (dec %))))
          roll (fn []
                 (rf/dispatch [:persist-dice-roll (merge @dice-roll
                                                          (action-rolls/generate-dice-results (:size @dice-roll)))])
                 (reset! dice-roll proto-dice-roll))
          position-and-effect-set? (fn [] (let [{:keys [position effect]} @dice-roll] (not-any? nil? [position effect])))
          position-effect-label (fn [position effect] (str (i18n/translate-position position locale) " ~ " (i18n/translate-effect effect locale)))
          on-mouse-over (fn [position effect] (reset! p-and-e-label (position-effect-label position effect)))
          on-mouse-out (fn [] (reset! p-and-e-label nil))
          on-click (fn [position effect] (swap! dice-roll assoc :position position :effect effect))]
      [:<>
       [:div {:class "dc-surface-bg grid grid-cols-3 grid-rows-2 p-1 pt-3"}
        [:div {:class "grid grid-cols-2"}
         [position-and-effect {:on-mouse-over on-mouse-over :on-mouse-out on-mouse-out :on-click on-click}]
         [:div {:class "w-64"}
          [:button {:class button-class
                    :on-click decrement} "-"]
          [:span {:class "align-middle prose prose-2xl" :style {:color "var(--dc-text)"}}(str (:size @dice-roll))]
          [:button {:class button-class
                    :on-click increment} "+"]]]
        [:div {:class "col-span-2 relative"}
         [:input {:type :text
                  :class (str text-input-class "")
                  :value (:text @dice-roll)
                  :placeholder (i18n/t :ui/roll-caption-placeholder locale)
                  :max-length "100"
                  :on-change (fn [^js e] (swap! dice-roll assoc :text (.. e -target -value)))}]
         [:button {:class (str "absolute inset-y-0 right-0 " button-class)
                   :on-click roll} (i18n/t :ui/roll locale)]]
        [:div]
        [:div {:class "col-span-2"}
         [:p {:class (str "mt-3 text-2xl"
                          (when-not (position-and-effect-set?) " dc-text-muted animate-pulse"))}
          (if (position-and-effect-set?)
            (let [{:keys [position effect]} @dice-roll] (position-effect-label position effect))
            @p-and-e-label)]
        ]
        ]]
       )))


(defn add-message []
  (r/with-let [new-message (r/atom nil)]
  (let [locale @(rf/subscribe [::locale/locale])]
  [:<>
         [:input {:type  :text
              :class text-input-class
              :value @new-message
              :placeholder (i18n/t :ui/message-placeholder locale)
              :max-length "100"
              :on-change
              (fn [^js e] (reset! new-message (.. e -target -value)))}]
     [:button {:disabled (string/blank? @new-message)
               :class button-class
               :on-click (fn []
                           (rf/dispatch [:send-message @new-message])
                           (reset! new-message nil))} (i18n/t :ui/send locale)]])))

(def content-box-class "container rounded-xl dc-panel-gradient")

(defn entry->entity
  "Firebase returns {id entity}; fold the id into the entity map."
  [[id entity]]
  (assoc entity :id id))

(defn messages-list []
  (let [messages (->> @(rf/subscribe [::subs/messages]) reverse (map entry->entity))]
  [:<>
  [:div {:class (str content-box-class " h-full flex flex-col min-h-0")}
  [:div {:class "p-2"} [roll-dice]]
  [:div {:class "flex flex-col flex-1 min-h-0"}
   [:div {:class "mx-2 p-2 dc-surface-bg"}
    [:span {:class "float-left w-full"} [:div {:class ""}[add-message]]]]
   [:div {:class "overscroll-auto overflow-auto flex-1 min-h-0 flex flex-col m-1 gap-1 p-1 dc-surface-bg"}

    (->> messages
         (remove (fn [{:keys [deleted?]}] deleted?))
         (map (fn [{:keys [id] :as message}] ^{:key id} [display-message message]))
         )]]]]
))

(def clock-button-class "px-1 text-3xl font-extra-bold")

(defn display-clock [clock]
  (let [channel @(rf/subscribe [::subs/channel])
        clocks-path (subs/clocks-path channel)
        {:keys [key tic id caption creator]} clock
        this-clock-path (conj clocks-path id)
        clock-face (clocks/get-face key tic)]
    [:div {:class "dc-surface-alt-bg relative"}
        [:div {:class "absolute top-2 right-4"}
         [:button {:class "print:hidden"
                   :on-click #(rf/dispatch [:mark-clock-deleted this-clock-path caption])} "x"]]
    [:div {:class "h-full m-px p-2 dc-surface-bg"}
     [:img  {:class "w-24 dc-clock-icon" :src (str "images/clocks/" clock-face)}]
     [:span {:class "inline-block print:hidden"}
      [:button {:class clock-button-class
                :disabled (not (< tic (clocks/max-index key)))
                :on-click #(rf/dispatch [:advance-clock this-clock-path clock])} "+"]
      [:button {:class clock-button-class
                :disabled (not (< 0 tic))
                :on-click #(rf/dispatch [:roll-back-clock this-clock-path clock])} "-"]]
     [:div {:class "text-lg prose prose-m" :style {:color "var(--dc-text)"}} caption]
     [:div {:class "text-xs"} creator]
     ]
     ]
))

(defn clocks-to-png-filter? [node]
  (let [tag-name (.. node -nodeName )]
    (not= tag-name "BUTTON")))
(def to-png-options (clj->js {:filter clocks-to-png-filter?}))

(defn clocks-to-png []
(let [clock-panel-div (. js/document (getElementById "clock-panel"))
      name @(rf/subscribe [::subs/name])
      channel @(rf/subscribe [::subs/channel])]
  (analytics/log-event :export-clocks-png {:channel_id channel :name name})
  (-> clock-panel-div
      (html-to-image/toPng to-png-options)
      (.then
       (fn [data-url] (downloadjs data-url "clocks.png")))
  )
))

; overscroll-auto overflow-auto max-h-screen grid m-1 gap-1 p-1
(defn display-clocks []
  (let [clocks (->> @(rf/subscribe [::subs/clocks]) reverse (map entry->entity))]
  [:div {:class (str content-box-class " flex-1 min-h-0 flex flex-col")}
   [:div {:class "p-2 flex-1 min-h-0 flex flex-col"}
     [:div {:class "dc-surface-bg p-3 flex-1 min-h-0 flex flex-col"}
   [:div {:class "overscroll-auto overflow-auto flex-1 min-h-0 print:container print:overflow-visible"
          }
    ; This div is specifically to support PNG downloads of clocks
    [:div {:class "grid grid grid-cols-3 flex relative dc-surface-bg" :id "clock-panel"}
        (->> clocks
         (remove (fn [{:keys [deleted?]}] deleted?))
         (map (fn [{:keys [id] :as clock}] ^{:key id} [display-clock clock])))]
    (when (< 0 (count clocks))
      [:div {:class "p-2"}
      [:a {:class "text-sm text-center print:hidden" :href "#"
           :on-click #(clocks-to-png)}
       [:i {:class "fas fa-camera"}]]])
    ]
  ]]]
))


(defn clocks-list []
  (r/with-let [caption (r/atom "")]
  (let [locale @(rf/subscribe [::locale/locale])
        clock-count (count @(rf/subscribe [::subs/clocks]))
        click-clock (fn [clock-key]
                       (rf/dispatch [:create-clock clock-key @caption clock-count])
                       (reset! caption ""))]
  [:div {:class (str content-box-class " h-full flex flex-col min-h-0")}
   [:div {:class "p-2 print:hidden"}
    [:div {:class "dc-surface-bg p-3"}
     [:input {:type  :text
              :class text-input-class
              :value @caption
              :placeholder (i18n/t :ui/clock-caption-placeholder locale)
              :max-length "100"
              :on-change
              (fn [^js e] (reset! caption (.. e -target -value)))}]
     [:div {:class "grid grid-cols-12 p-2"}
      (map (fn [{:keys [key face]}]
             ^{:key key} [:button {:on-click #(click-clock key)}
                          [:img {:class "w-8 dc-clock-icon" :src (str "images/clocks/" face)}]])
           clocks/clock-types)]]]
    [display-clocks]]
  )))

(defn enter-channel! []
  (let [name @(rf/subscribe [::subs/name])
        channel @(rf/subscribe [::subs/channel])]
    (analytics/log-event :enter-channel {:channel_id channel :name name})
    (rf/dispatch
     [::db/update {:value (cond-> {:last-accessed (.now js/Date)}
                            (config/preview-channel?) (assoc :test true))
                   :path (subs/channels-path channel)}])))

(defn channel-view
  "Mounted once per channel entry; `enter-channel!`'s side effects must fire
  exactly once here, not on every re-render triggered by new messages/clocks."
  []
  (r/create-class
   {:component-did-mount #(enter-channel!)
    :reagent-render
    (fn []
      [:div {:class "flex print:block flex-1 min-h-0"}
       [:div {:class "w-1/2 print:w-full print:hidden mr-2 min-h-0 flex flex-col"}
        [messages-list]]
       [:div {:class "w-1/2 print:w-full ml-2 min-h-0 flex flex-col"}
        [clocks-list]]])}))


(defn- apply-theme! [theme]
  (set! (.. js/document -documentElement -dataset -theme) theme))

(defn- apply-locale! [locale]
  (set! (.. js/document -documentElement -lang) locale))

(defn theme-toggle [theme locale]
  [:button {:class "dc-btn border-2 rounded px-2 py-1 text-xs print:hidden"
            :title (i18n/t :ui/theme-toggle-title locale)
            :on-click #(rf/dispatch [:toggle-theme theme])}
   (get subs/theme-label theme)])

(defn locale-toggle [locale]
  [:button {:class "dc-btn border-2 rounded px-2 py-1 text-xs print:hidden"
            :title (i18n/t :ui/locale-toggle-title locale)
            :on-click #(locale/set-locale! (if (= locale "ru") "en" "ru"))}
   (if (= locale "ru") "EN" "RU")])

(defn main-panel []
  (let [name @(rf/subscribe [::subs/name])
        user @(rf/subscribe [::auth/user-auth])
        db-connected? @(rf/subscribe [::db/realtime-value {:path [:.info :connected]}])
        channel @(rf/subscribe [::subs/channel])
        channel-name {:channel channel :name name}
        locale @(rf/subscribe [::locale/locale])
        ;; ::subs/theme opens a Firebase listener at subscribe-time; only
        ;; subscribe once auth+connectivity+channel are all resolved
        ;; (mirroring how messages/clocks live inside channel-view,
        ;; mounted under the same :else branch below) -- subscribing
        ;; earlier races anonymous sign-in and gets a one-time,
        ;; non-retrying permission_denied on the listener. ::locale/locale
        ;; is local-only (localStorage, not Firebase), so it has no such
        ;; race and can subscribe unconditionally here.
        theme (if (and user db-connected? (channel-name-ready? channel-name))
                @(rf/subscribe [::subs/theme])
                subs/default-theme)]
    (apply-theme! theme)
    (apply-locale! locale)
    [:div {:class "h-screen"}
     [:div {:class "flex flex-col w-full h-screen fixed pin-l pin-y dc-app-bg"}
      [:div {:class "grid grid-cols-3 mt-1"}
       [:div {:class "ml-1"}[:p>a {:class "float-left prose prose-xl dc-title-font" :href "/" :style {:color "var(--dc-text)"}} "Clocks and Dice"]]
       [:div {:class "text-sm text-center"}
        (when (channel-name-ready? channel-name)
          [:span
          [:p {:class "print:hidden"} (i18n/t :ui/copy-address-hint locale)]
          [:p {:class "font-mono"} (str utils/shareable-address)]])]
       [:div {:class "float-right text-right space-x-2"}
        (when (channel-name-ready? channel-name) [theme-toggle theme locale])
        [locale-toggle locale]
        [auth-display]]]
      [:div {:class "flex-1 min-h-0 flex flex-col"}
       (cond
         (not user)
         [:div {:class "container mx-auto flex flex-wrap content-center"}
          [intro-view/intro-view [auth-display]]]

         (not db-connected?)
         [:div (i18n/t :ui/loading locale)]

         (not (channel-name-ready? channel-name))
         [:div {:class "p-2"}
          [intro-view/intro-view
           [add-channel
            (fn [channel-name]
              (rf/dispatch [:channel-name channel-name]))]]]

         :else
         [:div {:class "p-2 flex-1 min-h-0 flex flex-col"} [channel-view]])]
      ]]))
