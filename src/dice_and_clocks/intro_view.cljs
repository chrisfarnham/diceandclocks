(ns dice-and-clocks.intro-view
  (:require [dice-and-clocks.clocks :as clocks]))

(def link-class "underline")

(defn intro-view [sign-in]
  [:div {:class "container mx-auto max-w-2xl px-4 py-8 text-center space-y-6"}

   [:p {:class "text-4xl dc-title-font"} "Clocks and Dice"]

   [:p "Clocks and Dice is an assistant (dice roller, chat, and clock tracker)
        for Evil Hat Productions' Blades in the Dark RPG."]

   [:div {:class "grid grid-cols-9 gap-1 justify-items-center"}
    (for [x (clocks/get-faces :eight-o)] ^{:key (str "intro-" x)}
         [:img {:class "w-6 dc-clock-icon" :src (str "images/clocks/" x)}])]

   [:div sign-in]

   [:p {:class "text-sm text-left"}
    "This site tends to work poorly in private browsing modes. You'll also want to allowlist
     this site for your ad-blocker. Your channel name isn't a password or a security boundary —
     it's just how you and your friends find the same game session instead of a stranger's.
     Pick something specific enough that no one stumbles onto it by guessing, and share it only
     with the people joining your game. This site is intended for casual use; please don't
     share sensitive information here."]

   [:div {:class "text-left space-y-4"}
    [:div
     [:p "Copyright 2021"]
     [:p "Chris Farnham "
      [:a {:class link-class :href "mailto:chris.farnham@gmail.com"} "chris.farnham@gmail.com"]]
     [:p [:a {:class link-class :href "https://www.paypal.com/paypalme/chrisfarnham"} "Donate"]]
     [:p "Source code available at "
      [:a {:class link-class :href "https://github.com/chrisfarnham/diceandclocks"} "github"]
      " under the MIT License"]]

    [:div
     [:p {:class "mb-2"} "Thanks to:"]
     [:ul {:class "list-inside list-disc"}
      [:li "SkyJedi's " [:a {:class link-class :href "https://dice.skyjedi.com/"} "Star Wars RPG game manager"] " for inspiration"]
      [:li [:a {:class link-class :href "https://acegiak.itch.io/"} "acegiak at itch.io"] " for the cool clock images"]
      [:li "Henry Widd's blog post, \"" [:a {:class link-class :href "https://widdindustries.com/clojurescript-firebase-simple/"} "Wrapper-free Firebase with Clojurescript's Re-Frame"] "\" for technical inspiration"]]]

    [:div {:class "text-center"}
     [:img {:class "w-24 mx-auto" :src "images/forged_in_the_dark_logo2_0.png"}]]

    [:div
     [:p "This work is based on Blades in the Dark (found at "
      [:a {:href "http://www.bladesinthedark.com/"} "http://www.bladesinthedark.com/"]
      "), product of One Seven Design, developed and authored by John Harper, and licensed for our use under the "
      [:a {:href "http://creativecommons.org/licenses/by/3.0/"} "Creative Commons Attribution 3.0 Unported license"] "."]]]])
