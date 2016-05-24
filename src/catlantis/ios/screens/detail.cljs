(ns catlantis.ios.screens.detail
  (:require [catlantis.shared.ui :as ui]
            [re-frame.core :as rf]
            [print.foo :as pf :include-macros true]
            [reagent.core :as r]))

(declare styles)

(def close-icon (js/require "./images/close.png"))
(def star-icon (js/require "./images/star.png"))
(def star-icon-full (js/require "./images/star_selected.png"))

(defn btn-icon [icon on-press tint-color]
  [ui/touchable-opacity
   {:on-press on-press
    :style    (:close-btn styles)}
   [ui/image
    {:source icon
     :style  {:tint-color (ui/color tint-color)}}]])

(def detail
  {:component
   (r/create-class
     {:reagent-render
      (fn []
        (let [detail (rf/subscribe [:detail])
              {:keys [image-selected random-fact]} @detail
              {:keys [url source-url id favorite?] :as image} image-selected]
          [ui/scroll-view
           {:style (:container styles)}
           [ui/view
            {:style (:buttons-wrap styles)}
            [btn-icon close-icon #(rf/dispatch [:nav/pop]) :white]
            [btn-icon (if favorite? star-icon-full star-icon)
             #(rf/dispatch [:image-favorite image favorite?]) :yellow700]]
           [ui/scroll-view
            {:maximum-zoom-scale 2.5}
            [ui/touchable-opacity
             {:on-press #(rf/dispatch [:nav/pop])}
             [ui/image-progress
              {:source      {:uri url}
               :resize-mode :contain
               :style       (:image-detail styles)}]]]
           [ui/view
            {:style (:text-wrap styles)}
            [ui/text
             {:style (:image-text styles)}
             random-fact]
            [ui/text
             {:on-press #(ui/open-url source-url)
              :style    (:source-link styles)}
             "Image Source"]]
           ]))})
   :config
   {:screen            :detail
    :screen-type       :light-box
    :title             ""
    :navigator-buttons {:right-buttons []
                        :left-buttons  [{:icon close-icon
                                         :id   :close}]}
    :style             {:background-blur "dark"}}})

(def styles
  (ui/create-stylesheet
    {:container    {:flex             1
                    :background-color :transparent
                    :flex-direction   :column}
     :text         {:color "white" :text-align "center" :font-weight "bold"}
     :image-detail {:flex       1
                    :height     "60%"
                    :width      "100%"
                    :margin-top 20}
     :buttons-wrap {:flex-direction  "row"
                    :justify-content :space-between
                    :margin-top      0
                    :padding-left    20
                    :padding-right   20}
     :text-wrap    {:justify-content :center
                    :align-items     :center
                    :margin-top      20}
     :source-link  {:text-align :right
                    :color      (ui/color :grey400)
                    :width      "90%"
                    :height     20
                    :font-size  12}
     :image-text   {:text-align :center
                    :color      (ui/color :white)
                    :width      "90%"
                    :height     "15%"}}))