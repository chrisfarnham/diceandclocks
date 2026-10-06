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

;; Inputs below are the shapes found in the production `sender`/`creator`
;; fields (snapshot 2026-10-06), with the long click-id tokens shortened.
(deftest strip-params-test
  (testing "plain names are unchanged"
    (is (= "Nick" (utils/strip-params "Nick")))
    (is (= "" (utils/strip-params ""))))
  (testing "name followed by a Facebook click id"
    (is (= "Nick" (utils/strip-params "Nick&fbclid=IwY2xjawUvQLdleHRuA2FlbQIxMABwZG9m_aem_pdZk1IWEeefi1fHix5SKcg"))))
  (testing "click-id prefixes seen in production (IwY2xj, IwAR, IwZXh0)"
    (is (= "Nick" (utils/strip-params "Nick&fbclid=IwAR2mBXabc-def_ghi")))
    (is (= "Nick" (utils/strip-params "Nick&fbclid=IwZXh0bgNhZW0CMTAAYnJpZBExabc"))))
  (testing "no name at all, only a click id"
    (is (= "" (utils/strip-params "fbclid=IwY2xjawUvSFBleHRuA2FlbQIxMABwZG9m"))))
  (testing "click id followed by a brid param"
    (is (= "" (utils/strip-params "fbclid=IwY2xjawUvSFBleHRu&brid=gJPH4R63abcDEF"))))
  (testing "utm_source from a ChatGPT link"
    (is (= "" (utils/strip-params "utm_source=chatgpt.com")))
    (is (= "Nick" (utils/strip-params "Nick&utm_source=chatgpt.com"))))
  (testing "ampersands that are part of a name survive, with or without params"
    (is (= "Hex & Wye" (utils/strip-params "Hex & Wye")))
    (is (= "Hex & Wye / Luyi" (utils/strip-params "Hex & Wye / Luyi")))
    (is (= "Aurel & Ancano" (utils/strip-params "Aurel & Ancano&fbclid=IwY2xjaw"))))
  (testing "question marks and parens in a name survive"
    (is (= "RIP Amosen Varren (or is he?)"
           (utils/strip-params "RIP Amosen Varren (or is he?)")))))
