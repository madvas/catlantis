(ns catlantis.ios.screens.favorites
  (:require [re-frame.core :as rf]
            [reagent.core :as r]
            [print.foo :as pf :include-macros true]
            [catlantis.ios.components.image-list :refer [image-list]]
            [catlantis.shared.ui :as ui]))

(declare styles)

(def favorites
  {:component
   (r/create-class
     {:component-will-mount
      (fn []
        (rf/dispatch-sync [:favorites-load]))
      :reagent-render
      (fn []
        (let [fav-query (rf/subscribe [:favorites])
              [images loading?] @fav-query]
          (if (seq images)
            [image-list images loading?]
            (if loading?
              [ui/activity-indicator-ios {:animating true}]
              [ui/view
               {:style (:no-imgs-wrap styles)}
               [ui/text {}
                "You haven't added any image to favorites yet"]]))))})
   :config
   {:screen            :favorites
    :screen-type       :screen
    :title             "Favorites"
    :navigator-buttons {:left-buttons
                        [{:icon (js/require "./images/back.png")
                          :id   :back}]}}
   :on-navigator-event-fn
   (fn [{:keys [id]}]
     (let [id (keyword id)]
       (case id
         :back (rf/dispatch [:nav/pop]))))})

(def styles
  (ui/create-stylesheet
    {:no-imgs-wrap {:flex        1
                    :padding-top 80
                    :align-items "center"}}))

