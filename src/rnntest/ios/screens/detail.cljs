(ns rnntest.ios.screens.detail
  (:require [rnntest.shared.ui :as ui]
            [rnntest.utils :as u]
            [re-frame.core :as rf]
            [print.foo :as pf :include-macros true]
            [reagent.core :as r]))

(declare styles)

(def detail
  {:component
   (r/create-class
     {:reagent-render
      (fn []
        (let []
          [ui/scroll-view {:style (:container styles)}
           [ui/text {:style (:text styles)} "Hola"]]))})
   :nav-config
   {:screen            :detail
    :screen-type       :modal
    :title             ""
    :navigator-buttons {:right-buttons []
                        :left-buttons  [{:icon (js/require "./images/close.png")
                                         :id   :close}]}}
   :on-navigator-event-fn
   (fn [screens navigator {:keys [id]}]
     (case (keyword id)
       :close (rf/dispatch [:nav/pop navigator])))})

(def styles
  (u/create-stylesheet
    {:container {:flex             1
                 :padding-top      80
                 :background-color (u/color :amber500)}
     :text      {:color "white" :text-align "center" :font-weight "bold"}}))
