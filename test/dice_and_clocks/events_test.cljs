(ns dice-and-clocks.events-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [re-frame.core :as rf]
            [re-frame.db :as rf-db]
            [dice-and-clocks.events]))

;; ::navigate's real handler calls `set!` on `js/window`'s location, which
;; would actually navigate the headless test browser away from the test
;; runner. Stub it to a no-op; the :db half of each event's effect map is
;; what's under test here, not browser navigation.
(rf/reg-fx :dice-and-clocks.events/navigate (fn [_]))

(deftest channel-name-event-test
  (testing "merges the slugified channel and name into db"
    (reset! rf-db/app-db {:existing :value})
    (rf/dispatch-sync [:channel-name {:channel "My Channel" :name "Alice"}])
    (is (= {:existing :value :channel "my-channel" :name "Alice"}
           @rf-db/app-db))))

(deftest channel-event-test
  (testing "sets db's channel key"
    (reset! rf-db/app-db {:existing :value})
    (rf/dispatch-sync [:channel "some-channel"])
    (is (= {:existing :value :channel "some-channel"}
           @rf-db/app-db))))

(deftest name-event-test
  (testing "sets db's name key"
    (reset! rf-db/app-db {:existing :value})
    (rf/dispatch-sync [:name "Bob"])
    (is (= {:existing :value :name "Bob"}
           @rf-db/app-db))))
