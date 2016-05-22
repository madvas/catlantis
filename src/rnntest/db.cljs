(ns rnntest.db
  (:require [schema.core :as s :include-macros true]
            [rnntest.ios.components.navigation :as nav]
            [rnntest.schemas :as sch]
            [rnntest.config :as cfg]))

;; schema of app-db
(def schema {:categories        (s/maybe [sch/Category])
             :category-selected (s/maybe sch/Category)
             :images-query      {:images   (s/maybe [sch/Image])
                                 :per-page s/Num
                                 :loading? s/Bool
                                 :category (s/maybe sch/Category)}
             :image-selected    (s/maybe sch/Image)
             :random-fact       s/Str
             :user              (s/maybe {:username s/Str})})

;; initial state of app-db
(def app-db {:categories        nil
             :category-selected nil
             :images-query      {:images   nil
                                 :per-page 5
                                 :loading? false
                                 :category nil}
             :image-selected    nil
             :random-fact       ""
             :user              {:username "mad"}})
