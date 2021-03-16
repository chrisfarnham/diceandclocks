(ns dice-and-clocks.intro-view
  (:require [dice-and-clocks.clocks :as clocks]))


(defn intro-view [sign-in]
  [:div {:class "container mx-auto"}
[:div {:class "overflow-auto grid grid-cols-3 gap-8"}

 [:div {:class ""}]
 [:div {:class ""} [:p {:class "text-4xl"} "Clocks and Dice"]]
 [:div {:class ""}]

 [:div {:class ""}]
 [:div {:class ""} [:p "Clocks and Dice is an assistant (dice roller, chat, and clock tracker) 
                        for Evil Hat Productions' Blades in the Dark RPG."]]
 [:div {:class ""}]

 [:div {:class ""}]
[:div {:class ""}
 [:div {:class "flex flex-row"}
  (for [x (clocks/get-faces :eight-o)] ^{:key (str "intro-" x)}
       [:div [:img {:class "w-8" :src (str "images/clocks/" x)}]])]]
[:div {:class ""}]


 [:div {:class ""}]
 [:div {:class ""} sign-in]
 [:div {:class ""}]

[:div {:class ""}]
[:div {:class "col-span-2 h-40 mr-8 overflow-scroll"} [:p {:class "text-sm"} "This site tends to work poorly in private browsing modes. 
                       You'll also want to whitelist this site for your ad-blocker."]
 [:p {:class "text-sm"} "Treat your channel name as if it were a password. It should be complex enough that no one
                         would guess it and only share it with friends joining your game."]
 [:p {:class "text-sm mt-2"} "Users should have no expectation of privacy or data safety. It is intended
                       for casual use and exchanging non-sensitive, unimportant information."]
 [:p {:class "text-sm mt-2"} "Consider any information
                       you post as if it were posted anonymously and publicly to the internet. Your email address is only used to
                       check that you are a real person and is not traceable to any dice rolls, clocks or messages
                       in the system. Dice roll, clocks and messages are identified by the channel
                       name chosen username for that session.
                       This site doesn't track identities (beyond making sure you have an account before joining the site)
                       or trace posted information to email addresses and accounts."]]
 


[:div {:class "col-span-3 grid grid-cols-1 gap-3 h-40 ml-8 overflow-scroll"}
 [:div {:class ""}
  [:div {:class "flex flex-col"}
  [:div {:class ""}"Copyright 2021"]
  [:div {:class ""}"Chris Farnham "  [:a {:class "underline" :href "mailto:chris.farnham@gmail.com"}]]
  [:div {:class ""}[:a {:class "underline" :href "https://www.paypal.com/paypalme/chrisfarnham"} "Donate"]]
  [:div {:class ""} "Source code available at "
   [:a {:class "underline" :href "https://github.com/chrisfarnham/diceandclocks"} "github"]
   " under the MIT License"]
  ]]

[:div {:class ""} [:p {:class "mb-2"}"Thanks to:"]
 [:ul {:class "list-inside list-disc"}
  [:li "SkyJedi's " [:a {:class "underline" :href "https://dice.skyjedi.com/"} "Star Wars RPG game manager"] " for inspiration"]
  [:li [:a {:class "underline" :href "https://acegiak.itch.io/"} "acegiak at itch.io"] " for the cool clock images"]
  [:li "Henry Widd's blog post, \""[:a {:class "underline" :href "https://widdindustries.com/clojurescript-firebase-simple/"} "Wrapper-free Firebase with Clojurescript's Re-Frame"] 
   "\" for technical inspiration"]
  ]
 ]


 [:div {:class "text-center"}
  [:img {:class "w-24" :src "images/forged_in_the_dark_logo2_0.png"}]]

 [:div  {:class "mr-8 mb-8"}
  [:p "This work is based on Blades in the Dark (found at " [:a {:href "http://www.bladesinthedark.com/"} "http://www.bladesinthedark.com/"], "product of One Seven Design, developed and authored by John Harper, and licensed for our use under the " [:a {:href "http://creativecommons.org/licenses/by/3.0/)"} "Creative Commons Attribution 3.0 Unported license"]]]
]
]]
)