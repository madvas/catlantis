(ns rnntest.ios.core
  (:require [reagent.core :as r :refer [atom]]
            [print.foo :as pf :include-macros true]
            [schema.core :as s :include-macros true]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [rnntest.handlers]
            [rnntest.subs]
            [rnntest.ios.components.navigation :as nav]
            [rnntest.utils :as u]
            [rnntest.ios.screens.home :refer [home]]
            [rnntest.ios.screens.detail :refer [detail]]
            [rnntest.ios.screens.categories :refer [categories]]
            [rnntest.ios.screens.favorites :refer [favorites]]
            [rnntest.ios.screens.user :refer [user]]))

(s/set-fn-validation! true)
(def nav-content-color (u/color :deep-orange500))

(defn init-nav []
  (nav/register-screen! home)
  (nav/register-screen! detail)
  (nav/register-screen! favorites)
  (nav/register-screen! user)
  (nav/register-reagent-component! :categories categories)
  (nav/start-single-screen-app!
    {:screen          :home
     :drawer          {:left {:screen :categories}}
     :persist-state?  true
     :animationType   :fade
     :navigator-style {:nav-bar-blur         true
                       ;:nav-bar-translucent true
                       :draw-under-nav-bar   true
                       :nav-bar-button-color nav-content-color
                       :nav-bar-text-color   nav-content-color}}))


(defn init []
  (dispatch-sync [:initialize-db])
  (init-nav))


