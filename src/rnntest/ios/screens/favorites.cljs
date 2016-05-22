(ns rnntest.ios.screens.favorites
  (:require [rnntest.utils :as u]
            [re-frame.core :as rf]
            [reagent.core :as r]
            [print.foo :as pf :include-macros true]
            [rnntest.config :refer [app-name]]
            [rnntest.ios.components.image-list :refer [image-list]]))

(declare styles)

(def favorites
  {:component
   (r/create-class
     {:component-will-mount
      (fn []
        (rf/dispatch [:favorites-load]))
      :reagent-render
      (fn []
        (let [fav-query (rf/subscribe [:favorites])
              [images loading?] @fav-query]
          [image-list images loading?]))})
   :config
   {:screen      :favorites
    :screen-type :screen
    :title       "Favorites"}
   :on-navigator-event-fn
   (fn [{:keys [id]}]
     (let [id (keyword id)]
       (case id
         :menu (rf/dispatch [:nav/toggle-drawer])
         (rf/dispatch [:nav/push id]))))})
