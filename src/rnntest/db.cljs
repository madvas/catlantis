(ns rnntest.db
  (:require [schema.core :as s :include-macros true]))


(s/defschema Category {:id   s/Str
                       :name s/Str})

(def o s/optional-key)

(s/defschema Image {:id             s/Str
                    :url            s/Str
                    (o :source-url) s/Str
                    (o :sub-id)     s/Str
                    (o :created)    s/Str
                    (o :favorite?)  s/Bool})

(s/defschema User {:username s/Str})

(s/defschema ImagesQuery
  {:images   (s/maybe [Image])
   :loading? s/Bool})

(def per-page 5)

;; schema of app-db
(def schema {:categories        (s/maybe [Category])
             :category-selected (s/maybe Category)
             :images-query      (merge ImagesQuery
                                       {:category (s/maybe Category)
                                        :per-page s/Num})
             :favorites-query   ImagesQuery
             :image-selected    (s/maybe Image)
             :random-fact       s/Str
             :user              (s/maybe User)})

;; initial state of app-db
(def app-db {:categories        nil
             :category-selected nil
             :images-query      {:images   nil
                                 :per-page per-page
                                 :loading? false
                                 :category nil}
             :favorites-query   {:images   nil
                                 :loading? false}
             :image-selected    nil
             :random-fact       ""
             :user              {:username "mad"}})
