(ns catlantis.db
  (:require [schema.core :as s :include-macros true]))


(s/defschema Category {:id   (s/maybe s/Str)
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

(def schema {:categories        [Category]
             :category-selected (s/maybe Category)
             :images-query      (merge ImagesQuery
                                       {:category (s/maybe Category)
                                        :per-page s/Num})
             :favorites-query   ImagesQuery
             :image-selected    (s/maybe Image)
             :random-fact       s/Str
             :user              (s/maybe User)})

(def app-db
  {:categories        [{:id nil :name "All Categories"}]
   :category-selected nil
   :images-query      {:images   nil
                       :per-page per-page
                       :loading? false
                       :category nil}
   :favorites-query   {:images   nil
                       :loading? false}
   :image-selected    nil
   :random-fact       ""
   :user              {:username ""}})
