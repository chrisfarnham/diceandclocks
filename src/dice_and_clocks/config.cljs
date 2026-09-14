(ns dice-and-clocks.config
  (:require [clojure.string :as string]))

(def debug?
  ^boolean goog.DEBUG)

(defn preview-channel?
  "True when served from a Firebase Hosting preview channel
  (<project-id>--<channel-id>-<hash>.web.app), as opposed to the live
  site or a custom domain."
  []
  (string/includes? (.. js/window -location -hostname) "--"))

(defn track-analytics? []
  (and (not debug?) (not (preview-channel?))))

