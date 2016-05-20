(ns rnntest.schemas
  (:require [schema.core :as s :include-macros true]))


(s/defschema Category {:id   s/Str
                       :name s/Str})

(s/defschema Image {:id         s/Str
                    :url        s/Str
                    :source-url s/Str})