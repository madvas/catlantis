(ns catlantis.ios.components.image-list
  (:require-macros [natal-shell.data-source :as ds])
  (:require [catlantis.shared.ui :as ui]
            [catlantis.utils :as u]
            [re-frame.core :as rf]
            [reagent.core :as r]
            [print.foo :as pf :include-macros true]
            [catlantis.config :refer [app-name]]))

(declare styles)

(def list-view-ds (ds/data-source {:rowHasChanged #(not= %1 %2)}))

(defn render-image [{:keys [url id] :as image}]
  [ui/touchable-opacity
   {:style          (:img-wrap styles)
    :active-opacity 0.8
    :on-press       #(rf/dispatch [:image-selected image])}
   [ui/image-progress
    {:style  (:image styles)
     :source {:uri url}
     :key    id}]])

(defn footer [loading?]
  (when loading?
    [ui/view
     {:style (:loading-wrap styles)}
     [ui/activity-indicator-ios
      {:style (:indicator styles)}]]))

(defn image-list
  ([images loading?] (image-list images loading? {}))
  ([images loading? attrs]
   (when images
     [ui/list-view (merge
                     {:dataSource    (ds/clone-with-rows list-view-ds images)
                      :render-row    (comp r/as-element render-image u/js->cljk)
                      :style         (:container styles)
                      :render-footer (comp r/as-element (partial footer loading?))}
                     attrs)])))

(def styles
  (ui/create-stylesheet
    {:container    {:flex             1
                    :background-color (ui/color :white)}
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