(ns rnntest.ios.screens.home
  (:require-macros [natal-shell.data-source :as ds])
  (:require [rnntest.shared.ui :as ui]
            [rnntest.utils :as u]
            [re-frame.core :as rf]
            [reagent.core :as r]
            [print.foo :as pf :include-macros true]
            [rnntest.config :refer [app-name]]
            [rnntest.ios.components.navigation :as nav]))

(declare styles)

(def list-view-ds (ds/data-source {:rowHasChanged #(not= %1 %2)}))

(defn render-image [{:keys [url source-url id] :as image}]
  [ui/touchable-opacity
   {:style          (:img-wrap styles)
    :active-opacity 0.8
    :on-press       #(rf/dispatch [:image-selected image])}
   [ui/image-progress
    {:style  (:image styles)
     :source {:uri url}
     :key    id}]])

(defn on-end-reached [ctg]
  (rf/dispatch [:images-load ctg]))

(defn footer [loading?]
  (when loading?
    [ui/view
     {:style (:loading-wrap styles)}
     [ui/activity-indicator-ios
      {:style (:indicator styles)}]]))

(def home-screen
  {:component
   (r/create-class
     {:reagent-render
      (fn []
        (let [ctg (rf/subscribe [:category-selected])
              img-query (rf/subscribe [:images @ctg])
              [images loading?] @img-query]
          (when images
            [ui/list-view {:dataSource     (ds/clone-with-rows list-view-ds images)
                           :render-row     (comp r/as-element render-image u/js->cljk)
                           :on-end-reached (partial on-end-reached @ctg)
                           :style          (:container styles)
                           :render-footer  (comp r/as-element (partial footer loading?))
                           }])
          ))})
   :nav-config
   {:screen            :home
    :screen-type       :screen
    :title             "Home"
    :navigator-buttons {:right-buttons
                        [{:title "Right"
                          :id    :some-screen}]
                        :left-buttons
                        [{:icon (js/require "./images/navicon_menu.png")
                          :id   :menu}]}}
   :on-navigator-event-fn
   (fn [registered-screens navigator {:keys [id]}]
     (case (keyword id)
       :menu (rf/dispatch [:nav/toggle-drawer navigator {:animated true}])
       (rf/dispatch [:nav/push navigator (get registered-screens
                                              (keyword id))])))})

(def styles
  (u/create-stylesheet
    {:container    {:flex             1
                    :background-color (u/color :white)}
     :text         {:color       "white"
                    :text-align  "center"
                    :font-weight "bold"}
     :image        {:width  "100%"
                    :height 300}
     :img-wrap     {:flex          1
                    :align-items   :center
                    :margin-bottom 10}
     :loading-wrap {:align-items   :center
                    :margin-bottom 10}
     :indicator    {}}))