(ns rnntest.ios.core
  (:require-macros [natal-shell.dimensions :as dim])
  (:require [reagent.core :as r :refer [atom]]
            [print.foo :as pf :include-macros true]
            [schema.core :as s :include-macros true]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [rnntest.handlers]
            [rnntest.subs]
            [rnntest.ios.components.navigation :as nav]
            [rnntest.shared.ui :as ui]
            [rnntest.utils :as u]
            [rnntest.ios.screens.home :as home]
            [rnntest.ios.screens.detail :as detail]
            [rnntest.ios.screens.categories :as categories]))

(s/set-fn-validation! true)

(defn init-nav []
  (nav/register-screen! home/home-screen)
  (nav/register-screen! detail/detail)
  (nav/register-reagent-component! :categories categories/categories)
  (nav/start-single-screen-app! {:screen              {:screen :home}
                                 :drawer              {:left {:screen :categories}}
                                 :use-last-nav-state? true
                                 :animationType       :fade}))


(defn init []
  (dispatch-sync [:initialize-db])
  (init-nav))


