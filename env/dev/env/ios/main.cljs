(ns ^:figwheel-no-load env.ios.main
  (:require [reagent.core :as r]
            [catlantis.ios.core :as core]
            [figwheel.client :as figwheel :include-macros true]
            [catlantis.utils :as u]
            [catlantis.ios.screens.home :as home]))

(enable-console-print!)
(aset js/console "disableYellowBox" true)

(def home (:component home/home))

(def cnt (r/atom 0))
(defn reloader []
  @cnt
  [home])


(def root-el (r/as-element [reloader]))

(figwheel/watch-and-reload
  :websocket-url "ws://localhost:6992/figwheel-ws"
  :heads-up-display false
  :jsload-callback (fn []
                     (u/clear-console!)
                     (core/init-nav)
                     (swap! cnt inc)))

(u/clear-console!)
(core/init)