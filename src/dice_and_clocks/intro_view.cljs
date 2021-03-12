(ns dice-and-clocks.intro-view
  (:require [dice-and-clocks.clocks :as clocks]))


(defn intro-view [sign-in]
[:div {:class "grid grid-cols-3 gap-8"}

 [:div {:class ""}]
 [:div {:class "text-center"} [:p {:class "text-4xl"} "Clocks and Dice"]]
 [:div {:class ""}]

 [:div {:class ""}]
 [:div {:class "text-center"} [:p "Clocks and Dice is an assistant (dice roller, chat, and clock tracker) for Evil Hat Productions' Blades in the Dark."]]
 [:div {:class ""}]

 [:div {:class ""}]
 [:div {:class "text-center"} sign-in]
 [:div {:class ""}]


 [:div {:class ""}]
 [:div {:class ""}
  [:div {:class "flex flex-row"} (for [x (clocks/get-faces :eight-o)] ^{:key (str "intro-" x)}[:div [:img {:class "w-10" :src (str "images/clocks/" x)}]])]]
 [:div {:class ""}]

 [:div {:class ""}]
 [:div {:class "text-center"}[:p [:a {:class "underline" :href "https://www.paypal.com/paypalme/chrisfarnham"} "Donate"]]]
 [:div {:class ""}]


 [:div {:class ""}]
 [:div {:class "text-center"}
  [:img {:class "w-24" :src "images/forged_in_the_dark_logo2_0.png"}]]
 [:div {:class ""}]

 [:div {:class ""}]
 [:div  {:class "text-center"}
  [:p "This work is based on Blades in the Dark (found at " [:a {:href "http://www.bladesinthedark.com/"} "http://www.bladesinthedark.com/"], "product of One Seven Design, developed and authored by John Harper, and licensed for our use under the " [:a {:href "http://creativecommons.org/licenses/by/3.0/)"} "Creative Commons Attribution 3.0 Unported license"]]]
 [:div {:class ""}]]
)