(ns rnntest.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [register-sub]]
            [rnntest.api :as api]
            [rnntest.config :as cfg]
            [re-frame.core :as rf]
            [print.foo :as pf :include-macros true]))

(register-sub
  :get-greeting
  (fn [db _]
    (reaction
      (get @db :greeting))))

(register-sub
  :categories
  (fn [db _]
    (reaction
      (if-let [categs (get @db :categories)]
        categs
        (do (api/fetch! :categories cfg/default-catapi-params
                        {:handler #(rf/dispatch [:categories-res %])})
            nil)))))

(register-sub
  :category-selected
  (fn [db _]
    (reaction
      (:category-selected @db))))


(register-sub
  :images
  (fn [db [_ req-category]]
    (reaction
      (let [{:keys [images loading? category]} (:images-query @db)]
        (if (or loading?
                (and images
                     (= category req-category)))
          [images loading?]
          (do (rf/dispatch-sync [:images-load req-category (not= category req-category)])
              [nil loading?]))))))


(register-sub
  :detail
  (fn [db _]
    (reaction
      (select-keys @db [:image-selected :random-fact]))))