(ns dice-and-clocks.views-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [dice-and-clocks.views :as views]))

(deftest entry->entity-test
  (testing "folds a Firebase {id entity} map entry's id into the entity map"
    (is (= {:id "-MVCos5aJx86-FvVlfxr" :sender "christopher" :size 0}
           (views/entry->entity ["-MVCos5aJx86-FvVlfxr" {:sender "christopher" :size 0}])))))
