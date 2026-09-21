(ns dice-and-clocks.subs-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [re-frame.core :as rf]
            [re-frame.db :as rf-db]
            [dice-and-clocks.subs :as subs]
            [dice-and-clocks.firebase-database :as db]))

;; ::messages/::clocks read through ::db/realtime-value, which in production
;; opens a real Firebase listener. Stub it to return the path it was asked
;; for, so these tests can assert *which path* gets subscribed to without
;; touching Firebase.
(rf/reg-sub ::db/realtime-value (fn [_db [_ args]] (:path args)))

(deftest channels-path-test
  (testing "builds a keyword-based channel path"
    (is (= [:channels :some-channel] (subs/channels-path "some-channel")))))

(deftest messages-path-test
  (testing "extends the channel path with :messages"
    (is (= [:channels :some-channel :messages] (subs/messages-path "some-channel")))))

(deftest clocks-path-test
  (testing "extends the channel path with :clocks"
    (is (= [:channels :some-channel :clocks] (subs/clocks-path "some-channel")))))

(deftest messages-sub-test
  (testing "subscribes to the current channel's messages path"
    (reset! rf-db/app-db {:channel "my-channel"})
    (is (= [:channels :my-channel :messages] @(rf/subscribe [::subs/messages]))))
  (testing "is nil when no channel is selected yet"
    (reset! rf-db/app-db {:channel ""})
    (is (nil? @(rf/subscribe [::subs/messages])))))

(deftest clocks-sub-test
  (testing "subscribes to the current channel's clocks path"
    (reset! rf-db/app-db {:channel "my-channel"})
    (is (= [:channels :my-channel :clocks] @(rf/subscribe [::subs/clocks]))))
  (testing "is nil when no channel is selected yet"
    (reset! rf-db/app-db {:channel ""})
    (is (nil? @(rf/subscribe [::subs/clocks])))))
