(ns rnntest.ios.screens.user
  (:require [rnntest.utils :as u]
            [re-frame.core :as rf]
            [reagent.core :as r]
            [print.foo :as pf :include-macros true]
            [rnntest.shared.ui :as ui]))

(declare styles)

(def bg-img1 (js/require "./images/login_bg1.jpg"))
(def bg-img2 (js/require "./images/login_bg2.jpg"))
(def bg-img3 (js/require "./images/login_bg3.jpg"))

(defn submit-user []
  )

(def user
  {:component
   (r/create-class
     {:reagent-render
      (fn []
        [ui/image
         {:source bg-img1
          :style  (:bg-img styles)}
         [ui/view {:style (:container styles)}
          [ui/text-input
           {:style                            (:input styles)
            :blur-on-submit                   true
            :on-change-text                   #()
            ;:value                            (:username state)
            :placeholder                      "Username"
            :enables-return-key-automatically true}]
          [ui/button {:on-press submit-user
                      :style    (:submit-btn styles)
                      :disabled true} "Submit"]
          [ui/keyboard-spacer {:animation-config (ui/anim-preset :spring)}]]])})
   :config
   {:screen          :user
    :screen-type     :modal
    :title           "User"
    :navigator-style {:nav-bar-hidden true}}
   :on-navigator-event-fn
   (fn [{:keys [id]}]
     (let [id (keyword id)]
       (case id
         :menu (rf/dispatch [:nav/toggle-drawer])
         (rf/dispatch [:nav/push id]))))})


(def styles
  (u/create-stylesheet
    {:bg-img     {:flex   1
                  :width  "100%"
                  :height "100%"}
     :container  {:background-color :transparent
                  :flex             1
                  :height           300
                  :justifyContent   :center}
     :input      {:height           50,
                  :background-color (u/color :white)
                  :width            "75%"
                  :margin-bottom    5
                  :border-radius    6
                  :align-self       :center
                  :opacity          0.75
                  }
     :submit-btn {:backgroundColor (u/color :cyan300)
                  :borderWidth     0
                  :width           "75%"
                  :opacity         0.9
                  :align-self      :center}}))