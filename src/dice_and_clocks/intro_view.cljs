(ns dice-and-clocks.intro-view
  (:require [dice-and-clocks.clocks :as clocks]
            [dice-and-clocks.i18n :as i18n]
            [dice-and-clocks.locale :as locale]
            [re-frame.core :as rf]))

(def link-class "underline")

(defn intro-view [sign-in]
  (let [locale @(rf/subscribe [::locale/locale])
        t #(i18n/t % locale)]
  [:div {:class "container mx-auto max-w-2xl px-4 py-8 text-center space-y-6"}

   [:p {:class "text-4xl dc-title-font"} "Clocks and Dice"]

   [:p (t :intro/description)]

   [:div {:class "grid grid-cols-9 gap-1 justify-items-center"}
    (for [x (clocks/get-faces :eight-o)] ^{:key (str "intro-" x)}
         [:img {:class "w-6 dc-clock-icon" :src (str "images/clocks/" x)}])]

   [:div sign-in]

   [:p {:class "text-sm text-left"} (t :intro/usage-warning)]

   [:div {:class "text-left space-y-4"}
    [:div
     [:p (t :intro/copyright)]
     [:p "Chris Farnham "
      [:a {:class link-class :href "mailto:chris.farnham@gmail.com"} "chris.farnham@gmail.com"]]
     [:p [:a {:class link-class :href "https://www.paypal.com/paypalme/chrisfarnham"} (t :intro/donate)]]
     [:p (t :intro/source-code-prefix)
      [:a {:class link-class :href "https://github.com/chrisfarnham/diceandclocks"} "github"]
      (t :intro/mit-license-suffix)]]

    [:div
     [:p {:class "mb-2"} (t :intro/thanks-heading)]
     [:ul {:class "list-inside list-disc"}
      [:li (t :intro/credit-skyjedi-prefix) [:a {:class link-class :href "https://dice.skyjedi.com/"} (t :intro/credit-skyjedi-link)] (t :intro/credit-skyjedi-suffix)]
      [:li [:a {:class link-class :href "https://acegiak.itch.io/"} (t :intro/credit-acegiak-link)] (t :intro/credit-acegiak-suffix)]
      [:li (t :intro/credit-widd-prefix) [:a {:class link-class :href "https://widdindustries.com/clojurescript-firebase-simple/"} (t :intro/credit-widd-link)] (t :intro/credit-widd-suffix)]]]

    [:div {:class "text-center"}
     [:img {:class "w-24 mx-auto" :src "images/forged_in_the_dark_logo2_0.png"}]]

    ;; This paragraph (the Blades in the Dark / CC BY 3.0 license
    ;; statement) is deliberately NOT routed through i18n -- it's a
    ;; legal/license-compliance statement, and an inaccurate or
    ;; ambiguous translation carries real risk for no real benefit.
    ;; Stays English-only in every locale.
    [:div
     [:p "This work is based on Blades in the Dark (found at "
      [:a {:href "http://www.bladesinthedark.com/"} "http://www.bladesinthedark.com/"]
      "), product of One Seven Design, developed and authored by John Harper, and licensed for our use under the "
      [:a {:href "http://creativecommons.org/licenses/by/3.0/"} "Creative Commons Attribution 3.0 Unported license"] "."]]]]))
