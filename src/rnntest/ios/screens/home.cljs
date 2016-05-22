(ns rnntest.ios.screens.home
  (:require [rnntest.utils :as u]
            [re-frame.core :as rf]
            [reagent.core :as r]
            [print.foo :as pf :include-macros true]
            [rnntest.config :refer [app-name]]
            [rnntest.config :as cfg]
            [rnntest.ios.components.image-list :refer [image-list]]))

(declare styles)

(def star-icon (js/require "./images/star.png"))

(defn on-end-reached [ctg]
  (rf/dispatch [:images-load ctg]))

(def home
  {:component
   (r/create-class
     {:reagent-render
      (fn []
        (let [ctg (rf/subscribe [:category-selected])
              img-query (rf/subscribe [:images @ctg])
              [images loading?] @img-query]
          [image-list images loading?
           {:on-end-reached (partial on-end-reached @ctg)}]))})
   :config
   {:screen            :home
    :screen-type       :screen
    :title             cfg/app-name
    :navigator-buttons {:right-buttons
                        [{:id   :favorites
                          :icon star-icon}]
                        :left-buttons
                        [{:icon (js/require "./images/navicon_menu.png")
                          :id   :menu}
                         {:icon (js/require "./images/user.png")
                          :id   :user}]}}
   :on-navigator-event-fn
   (fn [{:keys [id]}]
     (let [id (keyword id)]
       (case id
         :menu (rf/dispatch [:nav/toggle-drawer])
         (rf/dispatch [:nav/push id]))))})