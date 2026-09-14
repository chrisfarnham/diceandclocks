(ns dice-and-clocks.utils-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [dice-and-clocks.utils :as utils]))

(deftest slugify-test
  (testing "lowercases"
    (is (= "abc" (utils/slugify "ABC"))))
  (testing "replaces internal whitespace with a single hyphen"
    (is (= "foo-bar" (utils/slugify "foo bar"))))
  (testing "collapses multiple internal spaces into one hyphen"
    (is (= "foo-bar" (utils/slugify "foo   bar"))))
  (testing "trims leading and trailing whitespace before slugifying"
    (is (= "foo-bar" (utils/slugify "  foo bar  "))))
  (testing "strips punctuation"
    (is (= "foobar" (utils/slugify "foo!bar?"))))
  (testing "keeps existing hyphens and underscores"
    (is (= "foo-bar_baz" (utils/slugify "foo-bar_baz"))))
  (testing "empty string stays empty"
    (is (= "" (utils/slugify "")))))
