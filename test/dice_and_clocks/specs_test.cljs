(ns dice-and-clocks.specs-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [clojure.spec.alpha :as s]
            [dice-and-clocks.specs :as specs]))

(deftest clock-spec-test
  (testing "a real clock shape is valid"
    (is (s/valid? ::specs/clock
                   {:key :four-b :creator "chris" :caption "" :tic 0 :order 0})))
  (testing "id and deleted? are optional (only present once round-tripped through Firebase)"
    (is (s/valid? ::specs/clock
                   {:key :four-b :creator "chris" :caption "" :tic 0 :order 0
                    :id "-abc123" :deleted? true})))
  (testing "an unknown clock key is invalid"
    (is (not (s/valid? ::specs/clock
                        {:key :not-a-real-clock :creator "chris" :caption "" :tic 0 :order 0}))))
  (testing "a negative tic is invalid"
    (is (not (s/valid? ::specs/clock
                        {:key :four-b :creator "chris" :caption "" :tic -1 :order 0})))))

(deftest message-spec-test
  (testing "a plain chat message is valid"
    (is (s/valid? ::specs/message
                   {:message-type "message" :sender "chris" :text "hello"})))
  (testing "a plain chat message missing text is invalid"
    (is (not (s/valid? ::specs/message
                        {:message-type "message" :sender "chris"}))))
  (testing "a real dice-roll shape is valid"
    (is (s/valid? ::specs/message
                   {:message-type "dice-roll" :sender "chris"
                    :pool [3 4] :result 4 :size 2 :critical false})))
  (testing "a dice-roll result outside 1-6 is invalid"
    (is (not (s/valid? ::specs/message
                        {:message-type "dice-roll" :sender "chris"
                         :pool [3 4] :result 9 :size 2 :critical false}))))
  (testing "a size-1 roll with a 1-die pool is valid -- a single die is a
            normal, common roll per the SRD, not an edge case"
    (is (s/valid? ::specs/message
                   {:message-type "dice-roll" :sender "chris"
                    :pool [4] :result 4 :size 1 :critical false})))
  (testing "a size-0 roll (zero-dot action rating) is valid -- per the SRD's
            zero-dice rule, its pool still has 2 dice (roll two, take the
            lowest), it's just that :size itself, the action rating, is 0"
    (is (s/valid? ::specs/message
                   {:message-type "dice-roll" :sender "chris"
                    :pool [5 2] :result 2 :size 0 :critical false})))
  (testing "an empty pool is invalid"
    (is (not (s/valid? ::specs/message
                        {:message-type "dice-roll" :sender "chris"
                         :pool [] :result 5 :size 0 :critical false}))))
  (testing "size 9 (this app's UI-enforced maximum) is valid"
    (is (s/valid? ::specs/message
                   {:message-type "dice-roll" :sender "chris"
                    :pool (vec (repeat 9 4)) :result 4 :size 9 :critical false})))
  (testing "size 10 is invalid -- this app's dice-size +/- buttons clamp
            at 9, so a size of 10 can't come from a real client"
    (is (not (s/valid? ::specs/message
                        {:message-type "dice-roll" :sender "chris"
                         :pool (vec (repeat 10 4)) :result 4 :size 10 :critical false}))))
  (testing "a negative size is invalid"
    (is (not (s/valid? ::specs/message
                        {:message-type "dice-roll" :sender "chris"
                         :pool [4 2] :result 4 :size -1 :critical false}))))
  (testing "a real clock-event shape is valid"
    (is (s/valid? ::specs/message
                   {:message-type "clock-event" :sender "chris" :text "advanced a clock"
                    :key :four-b :tic 1 :caption "test clock"})))
  (testing "a real clock-deleted shape is valid"
    (is (s/valid? ::specs/message
                   {:message-type "clock-deleted" :sender "chris" :caption "test clock"})))
  (testing "an unrecognized message-type is invalid, not an exception"
    (is (not (s/valid? ::specs/message {:message-type "bogus" :sender "chris"})))))
