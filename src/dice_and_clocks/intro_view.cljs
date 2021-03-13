(ns dice-and-clocks.intro-view
  (:require [dice-and-clocks.clocks :as clocks]))


(defn intro-view [sign-in]
  [:div {:class "container mx-auto absolute"}
[:div {:class "overflow-auto grid grid-cols-3 gap-8"}

 [:div {:class ""}]
 [:div {:class ""} [:p {:class "text-4xl"} "Clocks and Dice"]]
 [:div {:class ""}]

 [:div {:class ""}]
 [:div {:class ""} [:p "Clocks and Dice is an assistant (dice roller, chat, and clock tracker) for Evil Hat Productions' Blades in the Dark."]]
 [:div {:class ""}]

 [:div {:class ""}]
 [:div {:class ""} sign-in]
 [:div {:class ""}]


 [:div {:class ""}]
 [:div {:class ""}
  [:div {:class "flex flex-row"} (for [x (clocks/get-faces :eight-o)] ^{:key (str "intro-" x)}[:div [:img {:class "w-10" :src (str "images/clocks/" x)}]])]]
 [:div {:class ""}]

 [:div {:class ""}]
 [:div {:class "col-span-2"}
  [:div {:class "flex flex-col"}
  [:div {:class "ml-8"}"Copyright 2021"]
  [:div {:class "ml-8"}"Chris Farnham "  [:a {:class "underline" :href "mailto:chris.farnham@gmail.com"}]]
  [:div {:class "ml-8 mb-8"}[:a {:class "underline" :href "https://www.paypal.com/paypalme/chrisfarnham"} "Donate"]]
  [:div "Source code available at "
   [:a {:class "underline" :href "https://github.com/chrisfarnham/diceandclocks"} "github"]
   " under the MIT License"]
  ]]

[:div {:class ""}]
[:div {:class "col-span-2"} [:p {:class "mb-2"}"Thanks to:"]
 [:ul {:class "list-inside list-disc"}
  [:li "SkyJedi's " [:a {:class "underline" :href "https://dice.skyjedi.com/"} "Star Wars RPG game manager"] " for inspiration"]
  [:li [:a {:class "underline" :href "https://acegiak.itch.io/"} "acegiak at itch.io"] " for the cool clock images"]
  [:li "Henry Widd's blog post, \""[:a {:class "underline" :href "https://widdindustries.com/clojurescript-firebase-simple/"} "Wrapper-free Firebase with Clojurescript's Re-Frame"] 
   "\" for technical inspiration"]
  ]
 ]


 [:div {:class ""}]
 [:div {:class "text-center"}
  [:img {:class "w-24" :src "images/forged_in_the_dark_logo2_0.png"}]]
 [:div {:class ""}]

 [:div {:class ""}]
 [:div  {:class "col-span-2 mr-8 mb-8"}
  [:p "This work is based on Blades in the Dark (found at " [:a {:href "http://www.bladesinthedark.com/"} "http://www.bladesinthedark.com/"], "product of One Seven Design, developed and authored by John Harper, and licensed for our use under the " [:a {:href "http://creativecommons.org/licenses/by/3.0/)"} "Creative Commons Attribution 3.0 Unported license"]]]

]]
)